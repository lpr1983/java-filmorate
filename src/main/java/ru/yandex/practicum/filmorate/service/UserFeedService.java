package ru.yandex.practicum.filmorate.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;

import java.time.Instant;
import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserFeedService {
    @Qualifier("feedDbStorage")
    private final FeedStorage feedStorage;

    public Feed create(Feed newFeed) {
        log.info("Create, input object {}", newFeed);
        Feed createdFeed = feedStorage.create(newFeed);
        log.info("Create, output object {}", createdFeed);
        return createdFeed;
    }

    public Collection<Feed> findAllByUserId(int userId) {
        log.debug("Getting feeds for userId={}", userId);

        return feedStorage.getFeeds(userId);
    }

    public void saveFeed(int userId, int entityId, @NotNull EventType eventType, @NotNull OperationType operation) {
        try {
            Feed newFeed = Feed.builder()
                    .userId(userId)
                    .entityId(entityId)
                    .eventType(eventType)
                    .operation(operation)
                    .timestamp(Instant.now())
                    .build();
            create(newFeed);
        } catch (Exception e) {
            log.error("Feed save error", e);
        }
    }
}
