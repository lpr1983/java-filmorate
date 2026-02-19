package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.OperationType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class FeedRowMapper implements RowMapper<Feed> {
    @Override
    public Feed mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        String eventType = resultSet.getString("event_type");
        String operation = resultSet.getString("operation");
        Timestamp timestamp = resultSet.getTimestamp("event_time");

        return Feed.builder()
                .eventId(resultSet.getInt("event_id"))
                .entityId(resultSet.getInt("entity_id"))
                .eventType(EventType.valueOf(eventType))
                .operation(OperationType.valueOf(operation))
                .userId(resultSet.getInt("user_id"))
                .timestamp(timestamp.toInstant())
                .build();
    }
}