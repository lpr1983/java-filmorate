package ru.yandex.practicum.filmorate.storage.recommendations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class RecommendationsDbStorage {
    private final NamedParameterJdbcTemplate jdbc;
    private final RowMapper<Film> mapper;
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

    public List<Film> getRecommendations(int userId) {
        log.info("getRecommendations userId={}", userId);

        // Id фильмов, пролайканных максимально похожими по лайкам пользователями, которые не смотрел userId.
        String recommendedQuery = """
                SELECT l.film_id AS film_id,
                       COUNT(*)  AS like_count
                FROM likes l
                LEFT JOIN likes my
                ON my.user_id = :userId AND my.film_id = l.film_id
                WHERE l.user_id IN (:mostSimilarUsersIds) AND my.film_id IS NULL
                GROUP BY l.film_id
                """;

        String queryStep2and3 = BASE_SELECT_FILMS_QUERY + """
                JOIN (
                """ + recommendedQuery + """
                ) recommended ON recommended.film_id = f.id
                ORDER BY recommended.like_count DESC, f.id
                """;

        List<Integer> mostSimilarUsersIds = getMostSimilarUsersIds(userId);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("mostSimilarUsersIds", mostSimilarUsersIds);

        return jdbc.query(queryStep2and3, params, mapper);
    }

    private List<Integer> getMostSimilarUsersIds(int userId) {

        String query = """
                SELECT l2.user_id AS user_id,
                       COUNT(*)   AS common_count
                FROM likes l1
                JOIN likes l2 ON l1.film_id = l2.film_id
                WHERE l1.user_id = :userId
                  AND l2.user_id <> :userId
                GROUP BY l2.user_id
                ORDER BY common_count DESC, l2.user_id;
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        List<Integer> mostSimilarUsersIds = jdbc.query(query, params, rs -> {
            List<Integer> result = new ArrayList<>();

            Integer maxCommonCount = null;
            while (rs.next()) {
                int count = rs.getInt("common_count");
                if (maxCommonCount == null) {
                    maxCommonCount = count;
                }
                if (count != maxCommonCount) {
                    break;
                }
                result.add(rs.getInt("user_id"));
            }
            return result;
        });

        if (mostSimilarUsersIds == null || mostSimilarUsersIds.isEmpty()) {
            return Collections.emptyList();
        }

        return mostSimilarUsersIds;
    }

}
