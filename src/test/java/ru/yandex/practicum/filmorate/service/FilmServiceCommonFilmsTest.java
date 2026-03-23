package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
class FilmServiceCommonFilmsTest {

    @Autowired
    private FilmService filmService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long userId;
    private Long friendId;
    private Long thirdUserId;

    private Long film1Id;
    private Long film2Id;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM review_reaction");
        jdbcTemplate.update("DELETE FROM reviews");
        jdbcTemplate.update("DELETE FROM user_like");
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM film_genre");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");

        userId = insertUser("user1@mail.com", "login1", "User One");
        friendId = insertUser("user2@mail.com", "login2", "User Two");
        thirdUserId = insertUser("user3@mail.com", "login3", "User Three");

        film1Id = insertFilm("Film 1", "Desc 1");
        film2Id = insertFilm("Film 2", "Desc 2");

        addLike(userId, film1Id);
        addLike(userId, film2Id);

        addLike(friendId, film1Id);
        addLike(friendId, film2Id);

        addLike(thirdUserId, film2Id);
    }

    @Test
    void shouldReturnCommonFilmsAsDto() {
        Collection<FilmDto> films = filmService.getCommonFilms(userId, friendId);

        assertThat(films).hasSize(2);
        assertThat(films)
                .extracting(FilmDto::getId)
                .containsExactly(film2Id, film1Id);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> filmService.getCommonFilms(99999L, friendId));
    }

    @Test
    void shouldThrowWhenFriendNotFound() {
        assertThrows(NotFoundException.class, () -> filmService.getCommonFilms(userId, 99999L));
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

    private void addLike(Long userId, Long filmId) {
        jdbcTemplate.update("INSERT INTO user_like (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }
}