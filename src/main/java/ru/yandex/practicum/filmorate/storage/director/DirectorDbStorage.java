package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.film.DirectorOfFilm;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class DirectorDbStorage extends BaseDbStorage<Director> {
    private final RowMapper<DirectorOfFilm> directorOfFilmRowMapper;

    public DirectorDbStorage(NamedParameterJdbcTemplate jdbc,
                             RowMapper<Director> mapper,
                             RowMapper<DirectorOfFilm> directorOfFilmRowMapper) {
        super(jdbc, mapper);
        this.directorOfFilmRowMapper = directorOfFilmRowMapper;
    }

    public Director create(Director director) {
        String createQuery = """
                INSERT INTO directors(name)
                VALUES (:name);
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", director.getName());

        int createdDirectorId = insertWithKeyReturning(createQuery, params);
        director.setId(createdDirectorId);

        return director;
    }

    public Director update(Director directorToUpdate) {
        String updateQuery = """
                UPDATE directors SET
                name = :name
                WHERE id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", directorToUpdate.getId())
                .addValue("name", directorToUpdate.getName());

        updateWithCheckResult(updateQuery, params);

        return directorToUpdate;
    }

    public void delete(int directorId) {
        String deleteQuery = """
                DELETE from directors
                WHERE id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", directorId);
        jdbc.update(deleteQuery, params);
    }

    public Optional<Director> getById(int id) {
        String query = """
                SELECT id,
                       name
                FROM directors
                WHERE id = :id
                """;
        return getOneById(query, id);
    }

    public List<Director> getAll() {
        String query = """
                SELECT id,
                       name
                FROM directors
                ORDER BY id
                """;

        return jdbc.query(query, mapper);
    }

    public List<Director> getDirectorsByIds(List<Integer> ids) {
        if (ids.isEmpty())
            return Collections.emptyList();

        String query = """
                SELECT * from directors
                WHERE id in (:ids)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ids", ids);

        return jdbc.query(query, params, mapper);
    }

    public void joinDirectorsToFilms(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        List<Integer> ids = films.stream()
                .map(Film::getId)
                .toList();

        Map<Integer, Set<Director>> directorsOfFilms = getDirectorsOfFilms(ids);

        for (Film film : films) {
            Integer filmId = film.getId();
            Set<Director> directors = directorsOfFilms.getOrDefault(filmId, Set.of());
            film.setDirectors(directors);
        }
    }

    private Map<Integer, Set<Director>> getDirectorsOfFilms(List<Integer> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        String query = """
                SELECT fd.film_id  AS film_id,
                       fd.director_id AS id,
                       d.name      AS name
                FROM film_directors fd
                JOIN directors d ON d.id = fd.director_id
                WHERE fd.film_id IN (:ids)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ids", ids);

        List<DirectorOfFilm> directorsOfFilms = jdbc.query(query, params, directorOfFilmRowMapper);

        Map<Integer, Set<Director>> result = new HashMap<>();
        for (DirectorOfFilm df : directorsOfFilms) {
            Integer filmId = df.getFilmId();

            result.computeIfAbsent(filmId,k -> new HashSet<>())
                    .add(df.getDirector());
        }
        return result;
    }
}
