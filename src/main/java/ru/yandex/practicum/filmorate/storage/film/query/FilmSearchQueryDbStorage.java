package ru.yandex.practicum.filmorate.storage.film.query;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSearchBy;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Repository
public class FilmSearchQueryDbStorage implements FilmSearchQueryStorage {
    private final NamedParameterJdbcTemplate jdbc;
    private final RowMapper<Film> mapper;
    private static final String BASE_SELECT_FILMS_QUERY = """
            SELECT films.id,
                   films.name,
                   films.description,
                   films.release_date,
                   films.duration,
                   films.mpa_rating_id AS mpa_rating_id,
                   m.name AS mpa_name,
                   m.age  AS mpa_age
            FROM films
            LEFT JOIN mpa_ratings m ON m.id = films.mpa_rating_id
            """;

    @Override
    public List<Film> search(String query, Set<FilmSearchBy> by) {
        String sqlQuery;
        if (by.contains(FilmSearchBy.DIRECTOR) && by.contains(FilmSearchBy.TITLE)) {
            sqlQuery = queryByTitleAndDirector();
        } else if (by.contains(FilmSearchBy.TITLE)) {
            sqlQuery = queryByTitle();
        } else if (by.contains(FilmSearchBy.DIRECTOR)) {
            sqlQuery = queryByDirector();
        } else {
            throw new IllegalArgumentException("Unknown search filter : " + by);
        }

        String pattern = "%" + query.toLowerCase() + "%";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("query", pattern);

        return jdbc.query(sqlQuery, params, mapper);
    }

    private String queryByTitle() {
        return BASE_SELECT_FILMS_QUERY + """
                JOIN
                (
                    SELECT q.id, COUNT(l.user_id) likes_amount FROM
                        (
                        SELECT f.id
                        FROM films f
                        WHERE LOWER(f.name) LIKE :query
                        ) q
                    LEFT JOIN likes l ON l.film_id = q.id
                    GROUP BY
                    q.id
                    ) filtered
                ON films.id = filtered.id
                ORDER BY filtered.likes_amount DESC, films.id
                """;
    }

    private String queryByDirector() {
        return BASE_SELECT_FILMS_QUERY + """
                JOIN
                (
                    SELECT q.id, COUNT(l.user_id) likes_amount FROM
                        (
                        SELECT f.id
                        FROM directors d
                        JOIN film_directors fd ON fd.director_id = d.id
                        JOIN films f ON fd.film_id = f.id
                        WHERE LOWER(d.name) LIKE :query
                        ) q
                    LEFT JOIN likes l ON l.film_id = q.id
                    GROUP BY
                    q.id
                    ) filtered
                ON films.id = filtered.id
                ORDER BY filtered.likes_amount DESC, films.id
                """;
    }

    private String queryByTitleAndDirector() {
        return BASE_SELECT_FILMS_QUERY + """
                JOIN
                (
                    SELECT q.id, COUNT(l.user_id) likes_amount FROM
                        (
                        SELECT f.id
                        FROM films f
                        WHERE LOWER(f.name) LIKE :query
                        UNION
                        SELECT f.id
                        FROM directors d
                        JOIN film_directors fd ON fd.director_id = d.id
                        JOIN films f ON fd.film_id = f.id
                        WHERE LOWER(d.name) LIKE :query
                        ) q
                    LEFT JOIN likes l ON l.film_id = q.id
                    GROUP BY
                    q.id
                    ) filtered
                ON films.id = filtered.id
                ORDER BY filtered.likes_amount DESC, films.id
                """;
    }
}