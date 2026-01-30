package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreOfFilm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class GenreDbStorage extends BaseDbStorage<Genre> {
    private final RowMapper<GenreOfFilm> genreOfFilmRowMapper;

    public GenreDbStorage(NamedParameterJdbcTemplate jdbc,
                          RowMapper<Genre> mapper,
                          RowMapper<GenreOfFilm> genreOfFilmRowMapper) {
        super(jdbc, mapper);
        this.genreOfFilmRowMapper = genreOfFilmRowMapper;
    }

    public List<Genre> getAll() {
        return jdbc.query("SELECT * from genres", mapper);
    }

    public Optional<Genre> getById(int id) {
        String query = """
                SELECT * from genres
                WHERE id = :id
                """;
        return getOneById(query, id);
    }

    public List<Genre> getGenresByIds(List<Integer> ids) {
        if (ids.isEmpty())
            return List.of();

        String query = """
                SELECT * from genres
                WHERE id in (:ids)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ids", ids);

        return jdbc.query(query, params, mapper);
    }

    public void joinGenresToFilms(List<Film> films) {
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

}
