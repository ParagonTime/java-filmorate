package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long userId;
    private Long filmId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM review_reaction");
        jdbcTemplate.update("DELETE FROM reviews");
        jdbcTemplate.update("DELETE FROM user_like");
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM film_genre");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");

        userId = insertUser("serviceuser@mail.com", "service_login", "Service User");
        filmId = insertFilm("Service Film", "Service Description");
    }

    @Test
    void shouldCreateReview() {
        NewReviewRequest request = new NewReviewRequest();
        request.setContent("Very good film");
        request.setIsPositive(true);
        request.setUserId(userId);
        request.setFilmId(filmId);

        var dto = reviewService.postReview(request);

        assertThat(dto).isNotNull();
        assertThat(dto.getReviewId()).isNotNull();
        assertThat(dto.getContent()).isEqualTo("Very good film");
        assertThat(dto.getUseful()).isEqualTo(0);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        NewReviewRequest request = new NewReviewRequest();
        request.setContent("Bad request");
        request.setIsPositive(true);
        request.setUserId(99999L);
        request.setFilmId(filmId);

        assertThrows(NotFoundException.class, () -> reviewService.postReview(request));
    }

    @Test
    void shouldThrowWhenFilmNotFound() {
        NewReviewRequest request = new NewReviewRequest();
        request.setContent("Bad request");
        request.setIsPositive(true);
        request.setUserId(userId);
        request.setFilmId(99999L);

        assertThrows(NotFoundException.class, () -> reviewService.postReview(request));
    }

    @Test
    void shouldThrowWhenCountIsZero() {
        assertThrows(ValidationException.class, () -> reviewService.getReviews(null, 0));
    }

    @Test
    void shouldThrowWhenCountIsNegative() {
        assertThrows(ValidationException.class, () -> reviewService.getReviews(null, -1));
    }

    @Test
    void shouldThrowWhenReviewNotFound() {
        assertThrows(NotFoundException.class, () -> reviewService.getReview(99999L));
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

