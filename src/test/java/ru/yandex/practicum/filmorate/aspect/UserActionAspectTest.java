package ru.yandex.practicum.filmorate.aspect;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.service.UserFeedService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Andrew Vilkov
 * @created 21.02.2026 - 20:14
 * @project java-filmorate
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:dummy;DB_CLOSE_DELAY=-1"
})
class UserActionAspectTest {

    @Mock
    private UserFeedService userFeedService;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private UserActionAspect aspect;

    @Test
    void testAddFriendActionSuccess() {
        int userId = 1;
        int friendId = 2;

        aspect.addFriendAction(userId, friendId);

        ArgumentCaptor<Feed> captor = ArgumentCaptor.forClass(Feed.class);
        verify(userFeedService).create(captor.capture());

        Feed feed = captor.getValue();
        assertEquals(userId, feed.getUserId());
        assertEquals(friendId, feed.getEntityId());
        assertEquals(EventType.FRIEND, feed.getEventType());
        assertEquals(OperationType.ADD, feed.getOperation());
    }

    @Test
    void testDeleteFriendActionSuccess() {
        int userId = 3;
        int friendId = 4;

        aspect.deleteFriendAction(userId, friendId);

        ArgumentCaptor<Feed> captor = ArgumentCaptor.forClass(Feed.class);
        verify(userFeedService).create(captor.capture());

        Feed feed = captor.getValue();
        assertEquals(userId, feed.getUserId());
        assertEquals(friendId, feed.getEntityId());
        assertEquals(EventType.FRIEND, feed.getEventType());
        assertEquals(OperationType.REMOVE, feed.getOperation());
    }

    @Test
    void testAddLikesActionSuccess() {
        int entityId = 10;
        int userId = 20;

        aspect.addLikesAction(entityId, userId);

        ArgumentCaptor<Feed> captor = ArgumentCaptor.forClass(Feed.class);
        verify(userFeedService).create(captor.capture());

        Feed feed = captor.getValue();
        assertEquals(userId, feed.getUserId());
        assertEquals(entityId, feed.getEntityId());
        assertEquals(EventType.LIKE, feed.getEventType());
        assertEquals(OperationType.ADD, feed.getOperation());
    }

    @Test
    void testDeleteLikesActionSuccess() {
        int entityId = 30;
        int userId = 40;

        aspect.deleteLikesAction(entityId, userId);

        ArgumentCaptor<Feed> captor = ArgumentCaptor.forClass(Feed.class);
        verify(userFeedService).create(captor.capture());

        Feed feed = captor.getValue();
        assertEquals(userId, feed.getUserId());
        assertEquals(entityId, feed.getEntityId());
        assertEquals(EventType.LIKE, feed.getEventType());
        assertEquals(OperationType.REMOVE, feed.getOperation());
    }

    @Test
    void testAddReviewActionSuccess() {
        Review review = new Review();
        review.setUserId(5);
        review.setReviewId(55);

        aspect.addReviewAction(review);

        ArgumentCaptor<Feed> captor = ArgumentCaptor.forClass(Feed.class);
        verify(userFeedService).create(captor.capture());

        Feed feed = captor.getValue();
        assertEquals(5, feed.getUserId());
        assertEquals(55, feed.getEntityId());
        assertEquals(EventType.REVIEW, feed.getEventType());
        assertEquals(OperationType.ADD, feed.getOperation());
    }

    @Test
    void testUpdateReviewActionSuccess() {
        Review review = new Review();
        review.setUserId(6);
        review.setReviewId(66);

        aspect.updateReviewAction(review);

        ArgumentCaptor<Feed> captor = ArgumentCaptor.forClass(Feed.class);
        verify(userFeedService).create(captor.capture());

        Feed feed = captor.getValue();
        assertEquals(6, feed.getUserId());
        assertEquals(66, feed.getEntityId());
        assertEquals(EventType.REVIEW, feed.getEventType());
        assertEquals(OperationType.UPDATE, feed.getOperation());
    }

    @Test
    void testDeleteReviewActionSuccess() {
        int reviewId = 77;
        Review review = new Review();
        review.setUserId(7);
        review.setReviewId(reviewId);

        when(reviewService.getReviewById(reviewId)).thenReturn(review);

        aspect.deleteReviewAction(reviewId);

        ArgumentCaptor<Feed> captor = ArgumentCaptor.forClass(Feed.class);
        verify(userFeedService).create(captor.capture());

        Feed feed = captor.getValue();
        assertEquals(7, feed.getUserId());
        assertEquals(reviewId, feed.getEntityId());
        assertEquals(EventType.REVIEW, feed.getEventType());
        assertEquals(OperationType.REMOVE, feed.getOperation());
    }

    @Test
    void testDeleteNonExistReviewActionNothingCreate() {
        int reviewId = 88;

        when(reviewService.getReviewById(reviewId)).thenThrow(new NotFoundException("Not found"));

        aspect.deleteReviewAction(reviewId);
        verify(userFeedService, never()).create(any());
    }

}