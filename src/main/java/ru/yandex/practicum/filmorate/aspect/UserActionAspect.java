package ru.yandex.practicum.filmorate.aspect;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.service.UserFeedService;

import java.time.Instant;


@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class UserActionAspect {
    private final UserFeedService userFeedService;
    private final ReviewService reviewService;

    @Pointcut("execution(* *..UserController.addFriend(int,int))")
    private void addFriendPointcut() {
    }

    @Pointcut("execution(* *..UserController.deleteFriend(int,int)) ")
    private void deleteFriendPointcut() {
    }

    @Pointcut("execution(* *..FilmController.addLike(int,int))")
    private void addLikesPointcut() {
    }

    @Pointcut("execution(* *..ReviewController.addLike(int,int))")
    private void addReviewLikesPointcut() {
    }

    @Pointcut("execution(* *..ReviewController.addDislike(int,int))")
    private void addReviewDislikePointcut() {
    }

    @Pointcut("execution(* *..FilmController.deleteLike(int,int))")
    private void deleteLikesPointcut() {
    }

    @Pointcut("execution(* *..ReviewController.deleteLike(int,int))")
    private void deleteReviewLikesPointcut() {
    }

    @Pointcut("execution(* *..ReviewController.deleteDislike(int,int))")
    private void deleteReviewDislikePointcut() {
    }

    @Pointcut("execution(* *..ReviewController.createReview(ru.yandex.practicum.filmorate.model.Review))")
    private void addReviewPointcut() {
    }

    @Pointcut("execution(* *..ReviewController.updateReview(ru.yandex.practicum.filmorate.model.Review))")
    private void updateReviewPointcut() {
    }

    @Pointcut("execution(* *..ReviewController.delete(int))")
    private void deleteReviewPointcut() {
    }

    @AfterReturning("addFriendPointcut() && args(id,friendId)")
    public void addFriendAction(int id, int friendId) {
        log.debug("Before addFriendAction execute, id {}, friendId {}", id, friendId);
        saveFeed(id, friendId, EventType.FRIEND, OperationType.ADD);
    }

    @AfterReturning("deleteFriendPointcut() && args(id,friendId)")
    public void deleteFriendAction(int id, int friendId) {
        log.debug("Before deleteFriendAction execute, id {}, friendId {}", id, friendId);
        saveFeed(id, friendId, EventType.FRIEND, OperationType.REMOVE);
    }

    @AfterReturning("(addLikesPointcut() || addReviewLikesPointcut() || addReviewDislikePointcut()) && args(id,userId)")
    public void addLikesAction(int id, int userId) {
        log.debug("Before addLikesAction execute, id {}, userId {}", id, userId);
        saveFeed(userId, id, EventType.LIKE, OperationType.ADD);
    }

    @AfterReturning("(deleteLikesPointcut() || deleteReviewLikesPointcut() || deleteReviewDislikePointcut()) && args(id,userId)")
    public void deleteLikesAction(int id, int userId) {
        log.debug("Before deleteLikesAction execute, id {}, userId {}", id, userId);
        saveFeed(userId, id, EventType.LIKE, OperationType.REMOVE);
    }

    @AfterReturning("addReviewPointcut() && args(review)")
    public void addReviewAction(Review review) {
        log.debug("Before addReviewAction execute, review {}", review);
        saveFeed(review.getUserId(), review.getReviewId(), EventType.REVIEW, OperationType.ADD);
    }

    @AfterReturning("updateReviewPointcut() && args(review)")
    public void updateReviewAction(Review review) {
        log.debug("Before updateReviewAction execute, review {}", review);
        saveFeed(review.getUserId(), review.getReviewId(), EventType.REVIEW, OperationType.UPDATE);
    }

    @Before("deleteReviewPointcut() && args(id)")
    public void deleteReviewAction(int id) {
        log.debug("Before deleteReviewFeedAction execute, review id {}", id);
        try {
            Review review = reviewService.getReviewById(id);
            saveFeed(review.getUserId(), review.getReviewId(), EventType.REVIEW, OperationType.REMOVE);
        } catch (NotFoundException e) {
            log.warn("Попытка удалить несуществующий отзыв c id {}", id);
        }
    }

    private void saveFeed(int userId, int entityId, @NotNull EventType eventType, @NotNull OperationType operation) {
        try {
            Feed newFeed = Feed.builder()
                    .userId(userId)
                    .entityId(entityId)
                    .eventType(eventType)
                    .operation(operation)
                    .timestamp(Instant.now())
                    .build();
            userFeedService.create(newFeed);
        } catch (Exception e) {
            log.error("Feed save error", e);
        }
    }
}
