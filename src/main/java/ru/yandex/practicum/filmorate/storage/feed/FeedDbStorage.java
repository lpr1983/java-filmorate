package ru.yandex.practicum.filmorate.storage.feed;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class FeedDbStorage extends BaseDbStorage<Feed> implements FeedStorage {

    public FeedDbStorage(NamedParameterJdbcTemplate jdbc, RowMapper<Feed> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Feed create(Feed feed) {
        String createQuery = """
                INSERT INTO user_feeds(entity_id, event_type, operation, user_id, event_time)
                VALUES (:entity_id, :event_type, :operation, :user_id, :event_time);
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("entity_id", feed.getEntityId())
                .addValue("event_type", feed.getEventType().name())
                .addValue("operation", feed.getOperation().name())
                .addValue("user_id", feed.getUserId())
                .addValue("event_time", Timestamp.from(feed.getTimestamp()));

        int createdId = insertWithKeyReturning(createQuery, params);
        feed.setEventId(createdId);
        return feed;
    }

    @Override
    public List<Feed> getFeeds(int userId) {
        String getByIdQuery = """
                SELECT * FROM user_feeds
                WHERE user_id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", userId);

        return jdbc.query(getByIdQuery, params, mapper);
    }
}
