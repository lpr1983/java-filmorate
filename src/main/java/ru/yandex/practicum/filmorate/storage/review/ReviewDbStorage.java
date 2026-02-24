package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DbStorageException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewDbStorage extends BaseDbStorage<Review> implements ReviewStorage {
    private static final String GET_REVIEW_BASE_QUERY = """
            SELECT
                r.id,
                r.content,
                r.positive,
                r.film_id,
                r.user_id,
                COALESCE(l.rating, 0) AS useful
            FROM
                reviews r
                LEFT JOIN (
                    SELECT
                       review_id,
                       sum(rating) AS rating
                    FROM
                        reviews_likes
                    GROUP BY
                        review_id
                    ) l on r.id = l.review_id
            """;

    private static final String GET_REVIEW_BY_ID_QUERY = GET_REVIEW_BASE_QUERY + """
            WHERE
                r.id = :id
            """;

    private static final String GET_ALL_REVIEWS_WITH_LIMIT_QUERY = GET_REVIEW_BASE_QUERY + """
            ORDER BY
                useful
            DESC LIMIT :count
            """;

    private static final String GET_ALL_REVIEWS_BY_FILM_ID_WITH_LIMIT_QUERY = GET_REVIEW_BASE_QUERY + """
            WHERE
                r.film_id = :film_id
            ORDER BY
                useful DESC
            LIMIT :count
            """;

    private static final String INSERT_REVIEW_QUERY = """
            INSERT INTO reviews(content, positive, film_id, user_id)
            VALUES (:content, :positive, :film_id, :user_id)
            """;

    private static final String UPDATE_REVIEW_QUERY = """
            UPDATE reviews SET
            content = :content, positive = :positive
            WHERE id = :id
            """;

    private static final String DELETE_REVIEW_QUERY = """
            DELETE FROM reviews
            WHERE id = :id
            """;

    private static final String ADD_LIKE_QUERY = """
            MERGE INTO reviews_likes(review_id, user_id, rating)
            KEY(review_id, user_id)
            VALUES (:review_id, :user_id, :rating)
            """;

    private static final String REMOVE_LIKE_QUERY = """
            DELETE FROM reviews_likes
            WHERE review_id = :review_id AND user_id = :user_id
            """;

    public ReviewDbStorage(NamedParameterJdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Optional<Review> getById(int reviewId) {
        return getOneById(GET_REVIEW_BY_ID_QUERY, reviewId);
    }

    @Override
    public Review create(Review review) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("positive", review.getIsPositive())
                .addValue("film_id", review.getFilmId())
                .addValue("user_id", review.getUserId());

        int id = insertWithKeyReturning(INSERT_REVIEW_QUERY, params);
        review.setReviewId(id);
        review.setUseful(0);
        return review;
    }

    @Override
    public Review update(Review review) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("positive", review.getIsPositive())
//                .addValue("film_id", review.getFilmId()) тесты "не хотят" обновление этих полей
//                .addValue("user_id", review.getUserId())
                .addValue("id", review.getReviewId());

        updateWithCheckResult(UPDATE_REVIEW_QUERY, params);

        return getOneById(GET_REVIEW_BY_ID_QUERY, review.getReviewId())
                .orElseThrow(() -> new DbStorageException("Ошибка получения обновленных данных"));
    }

    @Override
    public void delete(int reviewId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", reviewId);
        jdbc.update(DELETE_REVIEW_QUERY, params);
    }

    @Override
    public void addLike(int reviewId, int userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("review_id", reviewId)
                .addValue("user_id", userId)
                .addValue("rating", 1);

        jdbc.update(ADD_LIKE_QUERY, params);
    }

    @Override
    public void addDislike(int reviewId, int userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("review_id", reviewId)
                .addValue("user_id", userId)
                .addValue("rating", -1);

        jdbc.update(ADD_LIKE_QUERY, params);
    }

    @Override
    public void deleteLike(int reviewId, int userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("review_id", reviewId)
                .addValue("user_id", userId);

        jdbc.update(REMOVE_LIKE_QUERY, params);
    }

    @Override
    public List<Review> getAllReviews(int count) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("count", count);

        return jdbc.query(GET_ALL_REVIEWS_WITH_LIMIT_QUERY, params, mapper);
    }

    @Override
    public List<Review> getReviewsByFilmId(int filmId, int count) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("film_id", filmId)
                .addValue("count", count);

        return jdbc.query(GET_ALL_REVIEWS_BY_FILM_ID_WITH_LIMIT_QUERY, params, mapper);
    }
}
