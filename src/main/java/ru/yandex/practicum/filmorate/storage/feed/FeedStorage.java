package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;

public interface FeedStorage {
    Feed create(Feed feed);

    List<Feed> getFeeds(int userId);
}
