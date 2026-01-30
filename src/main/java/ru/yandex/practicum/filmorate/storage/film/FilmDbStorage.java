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
import java.util.List;
import java.util.Optional;

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

    public FilmDbStorage(NamedParameterJdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Film> getAll() {
        String query = BASE_SELECT_FILMS_QUERY
                + "\nORDER BY f.id";

        return jdbc.query(query, mapper);
    }

    @Override
    public Optional<Film> getById(int filmId) {
        String query = BASE_SELECT_FILMS_QUERY
                + "\nWHERE f.id = :id";

        return getOneById(query, filmId);
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

        return jdbc.query(popularQuery, params, mapper);
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
