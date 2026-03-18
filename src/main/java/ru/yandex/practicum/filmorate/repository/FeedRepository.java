package ru.yandex.practicum.filmorate.repository;


import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Feed;

import java.util.Collection;

@Slf4j
@Repository
public class FeedRepository extends BaseRepository<Feed> {
    private static final String INSERT_EVENT = "INSERT INTO events (user_id, timestamp, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_EVENTS_BY_USER_ID = "SELECT * FROM events WHERE user_id = ? ORDER BY timestamp ASC";


    public FeedRepository(JdbcTemplate jdbc, RowMapper<Feed> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Feed> getEventsByUserId(Long userId) {
        return findMany(SELECT_EVENTS_BY_USER_ID, userId);
    }

    public void addEvent(Feed feed) {
        try {
            insert(INSERT_EVENT,
                    feed.getUserId(),
                    feed.getTimestamp(),
                    feed.getEventType().name(),
                    feed.getOperation().name(),
                    feed.getEntityId());
            log.debug("Событие добавлено: {}", feed);
        } catch (Exception e) {
            log.error("Ошибка при добавлении события: {}", e.getMessage());
            throw e;
        }
    }


}