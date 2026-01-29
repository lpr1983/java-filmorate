package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository("filmDbStorage")
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String BASE_SELECT_FILMS_QUERY = """
            SELECT f.id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   f.mpa_rating_id AS mpa_rating_id,
                   m.name          AS mpa_name,
                   m.age           AS mpa_age
            FROM films f
            LEFT JOIN mpa_ratings m ON m.id = f.mpa_rating_id
            """;
    private final RowMapper<GenreOfFilm> genreOfFilmRowMapper;

    public FilmDbStorage(NamedParameterJdbcTemplate jdbc,
                         RowMapper<Film> mapper,
                         RowMapper<GenreOfFilm> genreOfFilmRowMapper
    ) {
        super(jdbc, mapper);
        this.genreOfFilmRowMapper = genreOfFilmRowMapper;
    }

    private void joinGenresToFilms(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        List<Integer> ids = films.stream()
                .map(Film::getId)
                .toList();

        Map<Integer, Set<Genre>> genresOfFilms = getGenresOfFilms(ids);

        for (Film film : films) {
            Integer filmId = film.getId();
            Set<Genre> genres = genresOfFilms.getOrDefault(filmId, Set.of());
            film.setGenres(genres);
        }
    }

    @Override
    public List<Film> getAll() {
        String query = BASE_SELECT_FILMS_QUERY
                + "\nORDER BY f.id";

        List<Film> films = jdbc.query(query, mapper);
        joinGenresToFilms(films);

        return films;
    }

    @Override
    public Optional<Film> getById(int filmId) {
        String query = BASE_SELECT_FILMS_QUERY
                + "\nWHERE f.id = :id";

        Optional<Film> result = getOneById(query, filmId);
        if (result.isPresent()) {
            Film film = result.get();
            joinGenresToFilms(List.of(film));
        }
        return result;
    }

    @Override
    public Film create(Film filmToCreate) {
        String createFilmQuery = """
                INSERT INTO films(name, description, release_date, duration, mpa_rating_id)
                VALUES (:name, :description, :release_date, :duration, :mpa_rating_id);
                """;
        MapSqlParameterSource params = paramsForCreation(filmToCreate);

        int id = insertWithKeyReturning(createFilmQuery, params);
        filmToCreate.setId(id);

        if (filmToCreate.getGenres() != null) {
            insertFilmGenres(filmToCreate);
        }

        return filmToCreate;
    }

    @Override
    public Film update(Film film) {
        String updateFilmQuery = """
                UPDATE films SET
                name          =  :name,
                description   =  :description,
                release_date  =  :release_date,
                duration      =  :duration,
                mpa_rating_id =  :mpa_rating_id
                WHERE id = :id
                """;
        MapSqlParameterSource params = paramsForCreation(film).addValue("id", film.getId());
        updateWithCheckResult(updateFilmQuery, params);

        String deleteFilmGenresQuery = """
                DELETE FROM film_genres
                WHERE film_id = :film_id
                """;
        jdbc.update(deleteFilmGenresQuery, new MapSqlParameterSource()
                .addValue("film_id", film.getId()));

        if (film.getGenres() != null) {
            insertFilmGenres(film);
        }

        return film;
    }

    @Override
    public void delete(int filmId) {
        String deleteQuery = """
                DELETE from films
                WHERE id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", filmId);
        jdbc.update(deleteQuery, params);
    }

    @Override
    public void addLike(int filmId, int userId) {
        String addLikeQuery = """
                INSERT INTO likes(film_id, user_id)
                VALUES (:filmId, :userId);
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);

        try {
            jdbc.update(addLikeQuery, params);
        } catch (DuplicateKeyException ex) {
            // Такой ключ уже есть
        }
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        String deleteLikeQuery = """
                DELETE FROM likes
                WHERE user_id = :userId AND film_id = :filmId
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);

        jdbc.update(deleteLikeQuery, params);
    }

    @Override
    public List<Film> getPopular(int count) {
        String popularQuery = BASE_SELECT_FILMS_QUERY
                + "\n" + """
                LEFT JOIN
                    (SELECT l.film_id AS film_id,
                    COUNT(l.user_id) AS amountOfLikes
                    FROM likes l
                    GROUP BY l.film_id) q
                ON q.film_id = f.id
                ORDER BY COALESCE(q.amountOfLikes, 0) DESC,
                         f.id
                LIMIT :count
                """;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("count", count);
        List<Film> films = jdbc.query(popularQuery, params, mapper);

        joinGenresToFilms(films);

        return films;
    }

    private Map<Integer, Set<Genre>> getGenresOfFilms(List<Integer> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        String query = """
                SELECT fg.film_id  AS film_id,
                       fg.genre_id AS id,
                       g.name      AS name
                FROM film_genres fg
                JOIN genres g ON g.id = fg.genre_id
                WHERE fg.film_id IN (:ids)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ids", ids);

        List<GenreOfFilm> genresOfFilms = jdbc.query(query, params, genreOfFilmRowMapper);

        Map<Integer, Set<Genre>> result = new HashMap<>();
        for (GenreOfFilm gf : genresOfFilms) {
            Integer filmId = gf.getFilmId();

            result.computeIfAbsent(filmId,k -> new HashSet<>())
                    .add(gf.getGenre());
        }
        return result;
    }

    private MapSqlParameterSource paramsForCreation(Film film) {

        LocalDate releaseDate = film.getReleaseDate();
        java.sql.Date sqlReleaseDate = null;
        if (releaseDate != null) {
            sqlReleaseDate = java.sql.Date.valueOf(film.getReleaseDate());
        }

        MpaRating mpa = film.getMpa();
        Integer mpaRatingId = null;
        if (mpa != null) {
            mpaRatingId = mpa.getId();
        }

        return new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("release_date", sqlReleaseDate)
                .addValue("duration", film.getDuration())
                .addValue("mpa_rating_id", mpaRatingId);
    }

    private void insertFilmGenres(Film film) {
        String insertGenres = """
                INSERT INTO film_genres(film_id, genre_id)
                VALUES (:film_id, :genre_id)
                """;
        for (Genre genre : film.getGenres()) {
            MapSqlParameterSource genresParams = new MapSqlParameterSource()
                    .addValue("film_id", film.getId())
                    .addValue("genre_id", genre.getId());

            updateWithCheckResult(insertGenres, genresParams);
        }
    }
}
