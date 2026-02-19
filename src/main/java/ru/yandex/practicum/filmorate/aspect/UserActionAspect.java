package ru.yandex.practicum.filmorate.aspect;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.annotation.SaveUserAction;
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

    @AfterReturning("@annotation(action) && args(id,userId)")
    public void filmFeedAction(int id, int userId, SaveUserAction action) {
        log.debug("Before filmFeedAction execute, id {}, userId {}", id, userId);
        saveFeed(userId, id, action.event(), action.operation());
    }

    @AfterReturning("@annotation(action) && args(id,friendId)")
    public void friendFeedAction(int id, int friendId, SaveUserAction action) {
        log.debug("Before friendFeedAction execute, id {}, friendId {}", id, friendId);
        saveFeed(id, friendId, action.event(), action.operation());
    }

    @AfterReturning("@annotation(action) && args(review)")
    public void reviewFeedAction(Review review, SaveUserAction action) {
        log.debug("Before reviewFeedAction execute, review {}", review);
        saveFeed(review.getUserId(), review.getReviewId(), action.event(), action.operation());
    }

    @Before("@annotation(action) && within(ru.yandex.practicum.filmorate.controller.ReviewController) && args(id)")
    public void deleteReviewFeedAction(int id, SaveUserAction action) {
        log.debug("Before deleteReviewFeedAction execute, review id {}", id);
        try {
            Review review = reviewService.getReviewById(id);
            saveFeed(review.getUserId(), review.getReviewId(), action.event(), action.operation());
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
