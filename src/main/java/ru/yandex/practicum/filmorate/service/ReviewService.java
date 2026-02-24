package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DbStorageException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserService userService;
    private final FilmService filmService;

    public ReviewService(ReviewStorage reviewStorage, UserService userService, FilmService filmService) {
        this.reviewStorage = reviewStorage;
        this.userService = userService;
        this.filmService = filmService;
    }

    public List<Review> getAllReviews(int count) {
        return reviewStorage.getAllReviews(count);
    }

    public List<Review> getReviewsByFilmId(int filmId, int count) {
        filmService.checkFilmExists(filmId);

        return reviewStorage.getReviewsByFilmId(filmId, count);
    }

    public Review getReviewById(int reviewId) {
        return reviewStorage.getById(reviewId)
                .orElseThrow(() -> new NotFoundException("Не найден отзыв с id: " + reviewId));
    }

    public Review create(Review review) {
        log.info("create review, input object {}", review);

        userService.checkUserExists(review.getUserId());
        filmService.checkFilmExists(review.getFilmId());

        int createdReviewId = reviewStorage.create(review).getReviewId();

        Review createdReview = reviewStorage.getById(createdReviewId)
                .orElseThrow(() -> new DbStorageException("Созденный отзыв не найден в БД, id: " + createdReviewId));

        log.info("create review, output object {}", createdReview);

        return createdReview;
    }

    public Review update(Review review) {
        log.info("update review, input object {}", review);

        userService.checkUserExists(review.getUserId());
        filmService.checkFilmExists(review.getFilmId());

        reviewStorage.update(review);

        int reviewId = review.getReviewId();
        Review updatedReview = reviewStorage.getById(reviewId)
                .orElseThrow(() -> new DbStorageException("Обновленный отзыв не найден в БД, id: " + reviewId));

        log.info("update review, output object {}", updatedReview);

        return updatedReview;
    }

    public void deleteById(int reviewId) {
        checkReviewExists(reviewId);

        log.info("delete review with id:{}", reviewId);

        reviewStorage.delete(reviewId);
    }

    public void addLike(int reviewId, int userId) {
        checkReviewExists(reviewId);
        userService.checkUserExists(userId);

        log.info("add like for review with id:{} from user with id:{}", reviewId, userId);

        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        checkReviewExists(reviewId);
        userService.checkUserExists(userId);

        log.info("add dislike for review with id:{} from user with id:{}", reviewId, userId);

        reviewStorage.addDislike(reviewId, userId);
    }

    public void deleteLike(int reviewId, int userId) {
        checkReviewExists(reviewId);
        userService.checkUserExists(userId);

        log.info("delete like for review with id:{} from user with id:{}", reviewId, userId);

        reviewStorage.deleteLike(reviewId, userId);
    }

    public void deleteDislike(int reviewId, int userId) {
        checkReviewExists(reviewId);
        userService.checkUserExists(userId);

        log.info("delete dislike for review with id:{} from user with id:{}", reviewId, userId);

        reviewStorage.deleteLike(reviewId, userId);
    }

    public void checkReviewExists(int reviewId) {
        reviewStorage.getById(reviewId).orElseThrow(() -> new NotFoundException("Не найден отзыв с id:" + reviewId));
    }
}
