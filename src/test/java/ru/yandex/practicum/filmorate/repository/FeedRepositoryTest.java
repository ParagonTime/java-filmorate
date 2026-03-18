package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.FeedEventType;
import ru.yandex.practicum.filmorate.model.FeedOperationType;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase

public class FeedRepositoryTest {

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long user1Id;
    private Long user2Id;
    private Long film1Id;

    @BeforeEach
    void prepare() {
        jdbcTemplate.update("DELETE FROM events");
        jdbcTemplate.update("DELETE FROM user_like");
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM film_genre");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");

        user1Id = insertUser("user1@mail.ru", "user1_login", "User First");
        user2Id = insertUser("user2@mail.ru", "user2_login", "User Second");

        film1Id = insertFilm("Test Film 1", "Test Film 1 Description");
    }

    @Test
    void checkCreateAndGetFeed4AddFriend() {
        Instant currentTimestamp = Instant.now();
        long milliseconds = currentTimestamp.toEpochMilli();
        Feed feed = makeFeed(user1Id, milliseconds, FeedEventType.FRIEND, FeedOperationType.ADD, user2Id);
        feedRepository.addEvent(feed);

        Collection<Feed> c = feedRepository.getEventsByUserId(user1Id);
        assertThat(c.size()).isEqualTo(1);
        ArrayList<Feed> al = new ArrayList<>(c);
        assertThat(al.get(0).getUserId()).isEqualTo(user1Id);
        assertThat(al.get(0).getTimestamp()).isEqualTo(milliseconds);
        assertThat(al.get(0).getEventType()).isEqualTo(FeedEventType.FRIEND);
        assertThat(al.get(0).getOperation()).isEqualTo(FeedOperationType.ADD);
        assertThat(al.get(0).getEntityId()).isEqualTo(user2Id);
    }

    @Test
    void checkCreateAndGetFeed4UpdFilmLike() {
        Instant currentTimestamp = Instant.now();
        long milliseconds = currentTimestamp.toEpochMilli();
        Feed feed = makeFeed(user1Id, milliseconds, FeedEventType.LIKE, FeedOperationType.UPDATE, film1Id);
        feedRepository.addEvent(feed);

        Collection<Feed> c = feedRepository.getEventsByUserId(user1Id);
        assertThat(c.size()).isEqualTo(1);
        ArrayList<Feed> al = new ArrayList<>(c);
        assertThat(al.get(0).getUserId()).isEqualTo(user1Id);
        assertThat(al.get(0).getTimestamp()).isEqualTo(milliseconds);
        assertThat(al.get(0).getEventType()).isEqualTo(FeedEventType.LIKE);
        assertThat(al.get(0).getOperation()).isEqualTo(FeedOperationType.UPDATE);
        assertThat(al.get(0).getEntityId()).isEqualTo(film1Id);
    }



    private Feed makeFeed(Long userId, Long timestamp, FeedEventType eventType, FeedOperationType operation, Long entityId) {
        Feed feed = new Feed();
        feed.setUserId(userId);
        feed.setTimestamp(timestamp);
        feed.setEventType(eventType);
        feed.setOperation(operation);
        feed.setEntityId(entityId);
        return feed;
    }

    private Long insertUser(String email, String login, String name) {
        String sql = """
                INSERT INTO users (email, login, name, birthday)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, email);
            ps.setString(2, login);
            ps.setString(3, name);
            ps.setDate(4, Date.valueOf("2000-01-01"));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    private Long insertFilm(String name, String description) {
        String sql = """
                INSERT INTO films (name, description, release_date, duration, rating_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setDate(3, Date.valueOf("2020-01-01"));
            ps.setInt(4, 120);
            ps.setLong(5, 1);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }


}
