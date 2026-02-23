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

    public List<Film> getRecommendations(int userId) {
        log.info("getRecommendations userId={}", userId);

//        Из ТЗ:
//        1) Найти пользователей с максимальным количеством пересечения по лайкам.
//        2) Определить фильмы, которые один пролайкал, а другой нет.
//        3) Рекомендовать фильмы, которым поставил лайк пользователь с похожими вкусами,
//        а тот, для кого составляется рекомендация, ещё не поставил.

        // Делается 2мя запросами с промежуточной обработкой, т.к.
        // А) Один запрос с подзапросами очень плохо читается.
        // Б) Логика определения пользователей с максимальным количеством лайков может измениться.
        // В) Не с WITH т.к. в режиме H2 с текущей конфигурацией работало неожиданно по сравнению с подзапросом.
        // Запрос возвращало 0 строк при наличии пересечений. При этом в dbBeaver запрос отрабатывал корректно.

        // Шаг 1. Найти пользователей с максимальным количеством пересечения по лайкам.
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        String queryStep1 = """
                SELECT l2.user_id AS user_id,
                       COUNT(*)   AS common_count
                FROM likes l1
                JOIN likes l2 ON l1.film_id = l2.film_id
                WHERE l1.user_id = :userId
                  AND l2.user_id <> :userId
                GROUP BY l2.user_id
                ORDER BY common_count DESC, l2.user_id;
                """;

        List<Integer> maxCommonUsersIds = jdbc.query(queryStep1, params, rs -> {
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

        // Если нет пользователей с пересечениями — рекомендаций нет
        if (maxCommonUsersIds == null || maxCommonUsersIds.isEmpty()) {
            return Collections.emptyList();
        }

        params.addValue("maxCommonUsersIds", maxCommonUsersIds);

        // Шаг 2,3. Определить фильмы, которые один пролайкал, а другой нет.
        // Рекомендовать фильмы, которым поставил лайк пользователь с похожими вкусами,
        // а тот, для кого составляется рекомендация, ещё не поставил.
        String baseFilmQuery = """
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

        String recommendedQuery = """
        SELECT
            l.film_id,
            COUNT(*) AS like_count
        FROM likes l
        LEFT JOIN likes my
               ON my.user_id = :userId
              AND my.film_id = l.film_id
        WHERE l.user_id IN (:maxCommonUsersIds)
          AND my.film_id IS NULL
        GROUP BY l.film_id
        """;

        String queryStep2and3 = baseFilmQuery + """
        JOIN (
        """ + recommendedQuery + """
        ) recommended ON recommended.film_id = f.id
        ORDER BY recommended.like_count DESC, f.id
        """;

        return jdbc.query(queryStep2and3, params, mapper);
    }
}
