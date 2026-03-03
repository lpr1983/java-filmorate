package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Optional<Review> getById(int reviewId);

    Review create(Review review);

    Review update(Review review);

    void delete(int reviewId);

    void addLike(int reviewId, int userId);

    void addDislike(int reviewId, int userId);

    void deleteLike(int reviewId, int userId);

    List<Review> getAllReviews(int count);

    List<Review> getReviewsByFilmId(int filmId, int count);
}
