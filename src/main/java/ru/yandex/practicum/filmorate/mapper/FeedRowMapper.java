package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.FeedEventType;
import ru.yandex.practicum.filmorate.model.FeedOperationType;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedRowMapper implements RowMapper<Feed> {
    @Override
    public Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
        Feed feed = new Feed();
        feed.setEventId(rs.getLong("event_id"));
        feed.setUserId(rs.getLong("user_id"));
        feed.setTimestamp(rs.getLong("timestamp"));
        feed.setEntityId(rs.getLong("entity_id"));
        feed.setEventType(FeedEventType.valueOf(rs.getString("event_type")));
        feed.setOperation(FeedOperationType.valueOf(rs.getString("operation")));
        return feed;
    }
}