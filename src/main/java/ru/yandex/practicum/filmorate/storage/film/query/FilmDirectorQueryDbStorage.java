package ru.yandex.practicum.filmorate.storage.film.query;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.DirectorSortBy;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class FilmDirectorQueryDbStorage {
    protected final NamedParameterJdbcTemplate jdbc;
    protected final RowMapper<Film> mapper;

    public List<Film> getFilmsByDirector(int directorId, DirectorSortBy sortBy) {

        String query = switch (sortBy) {
            case YEAR -> """
                    SELECT f.id,
                           f.name,
                           f.description,
                           f.release_date,
                           f.duration,
                           f.mpa_rating_id AS mpa_rating_id,
                           m.name          AS mpa_name,
                           m.age           AS mpa_age
                    FROM films f
                    JOIN film_directors fd ON f.id = fd.film_id
                    LEFT JOIN mpa_ratings m ON m.id = f.mpa_rating_id
                    WHERE fd.director_id = :director_id
                    ORDER BY f.release_date, f.id
                    """;
            case LIKES -> """
                      SELECT f.id,
                           f.name,
                           f.description,
                           f.release_date,
                           f.duration,
                           f.mpa_rating_id AS mpa_rating_id,
                           m.name          AS mpa_name,
                           m.age           AS mpa_age
                    FROM films f
                    JOIN film_directors fd ON f.id = fd.film_id
                    LEFT JOIN mpa_ratings m ON m.id = f.mpa_rating_id
                    LEFT JOIN (
                        SELECT l.film_id,
                               COUNT(l.user_id) AS likes_amount
                        FROM likes l
                        JOIN film_directors fd2 ON fd2.film_id = l.film_id
                        WHERE fd2.director_id = :director_id
                        GROUP BY l.film_id
                    ) lc ON lc.film_id = f.id
                    WHERE fd.director_id = :director_id
                    ORDER BY COALESCE(lc.likes_amount, 0) DESC, f.id
                    """;
        };

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("director_id", directorId);

        return jdbc.query(query, params, mapper);
    }
}