package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long userId;
    private Long secondUserId;
    private Long thirdUserId;
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

        userId = insertUser("user1@mail.com", "login1", "User One");
        secondUserId = insertUser("user2@mail.com", "login2", "User Two");
        thirdUserId = insertUser("user3@mail.com", "login3", "User Three");

        filmId = insertFilm("Test Film", "Test Description");
    }

    @Test
    void shouldCreateAndGetReview() {
        Review review = makeReview("Great film", true, userId, filmId);

        Review saved = reviewRepository.save(review);
        Review found = reviewRepository.getReview(saved.getReviewId());

        assertThat(found).isNotNull();
        assertThat(found.getReviewId()).isEqualTo(saved.getReviewId());
        assertThat(found.getContent()).isEqualTo("Great film");
        assertThat(found.getIsPositive()).isTrue();
        assertThat(found.getUserId()).isEqualTo(userId);
        assertThat(found.getFilmId()).isEqualTo(filmId);
        assertThat(found.getUseful()).isEqualTo(0);
    }

    @Test
    void shouldUpdateReview() {
        Review review = makeReview("Bad film", false, userId, filmId);
        review = reviewRepository.save(review);

        review.setContent("Actually good");
        review.setIsPositive(true);

        reviewRepository.update(review);

        Review updated = reviewRepository.getReview(review.getReviewId());

        assertThat(updated.getContent()).isEqualTo("Actually good");
        assertThat(updated.getIsPositive()).isTrue();
    }

    @Test
    void shouldDeleteReview() {
        Review review = makeReview("Delete me", true, userId, filmId);
        review = reviewRepository.save(review);

        boolean deleted = reviewRepository.deleteById(review.getReviewId());

        assertThat(deleted).isTrue();
        assertThat(reviewRepository.getReviews(null, 10)).isEmpty();
    }

    @Test
    void shouldAddLikeAndIncreaseUseful() {
        Review review = makeReview("Like test", true, userId, filmId);
        review = reviewRepository.save(review);

        reviewRepository.addReaction(review.getReviewId(), secondUserId, 1);

        Review updated = reviewRepository.getReview(review.getReviewId());

        assertThat(updated.getUseful()).isEqualTo(1);
    }

    @Test
    void shouldAddDislikeAndDecreaseUseful() {
        Review review = makeReview("Dislike test", true, userId, filmId);
        review = reviewRepository.save(review);

        reviewRepository.addReaction(review.getReviewId(), secondUserId, -1);

        Review updated = reviewRepository.getReview(review.getReviewId());

        assertThat(updated.getUseful()).isEqualTo(-1);
    }

    @Test
    void shouldReplaceDislikeWithLike() {
        Review review = makeReview("Reaction replace test", true, userId, filmId);
        review = reviewRepository.save(review);

        reviewRepository.addReaction(review.getReviewId(), secondUserId, -1);
        assertThat(reviewRepository.getReview(review.getReviewId()).getUseful()).isEqualTo(-1);

        reviewRepository.addReaction(review.getReviewId(), secondUserId, 1);
        assertThat(reviewRepository.getReview(review.getReviewId()).getUseful()).isEqualTo(1);
    }

    @Test
    void shouldDeleteLike() {
        Review review = makeReview("Delete like test", true, userId, filmId);
        review = reviewRepository.save(review);

        reviewRepository.addReaction(review.getReviewId(), secondUserId, 1);
        assertThat(reviewRepository.getReview(review.getReviewId()).getUseful()).isEqualTo(1);

        reviewRepository.deleteReaction(review.getReviewId(), secondUserId, 1);

        Review updated = reviewRepository.getReview(review.getReviewId());
        assertThat(updated.getUseful()).isEqualTo(0);
    }

    @Test
    void shouldDeleteDislike() {
        Review review = makeReview("Delete dislike test", true, userId, filmId);
        review = reviewRepository.save(review);

        reviewRepository.addReaction(review.getReviewId(), secondUserId, -1);
        assertThat(reviewRepository.getReview(review.getReviewId()).getUseful()).isEqualTo(-1);

        reviewRepository.deleteReaction(review.getReviewId(), secondUserId, -1);

        Review updated = reviewRepository.getReview(review.getReviewId());
        assertThat(updated.getUseful()).isEqualTo(0);
    }

    @Test
    void shouldReturnReviewsSortedByUseful() {
        Review first = makeReview("Review 1", true, userId, filmId);
        Review second = makeReview("Review 2", true, userId, filmId);

        first = reviewRepository.save(first);
        second = reviewRepository.save(second);

        reviewRepository.addReaction(second.getReviewId(), secondUserId, 1);
        reviewRepository.addReaction(second.getReviewId(), thirdUserId, 1);

        Collection<Review> reviews = reviewRepository.getReviews(filmId, 10);

        assertThat(reviews).hasSize(2);
        assertThat(reviews.iterator().next().getReviewId()).isEqualTo(second.getReviewId());
    }

    private Review makeReview(String content, boolean isPositive, Long userId, Long filmId) {
        Review review = new Review();
        review.setContent(content);
        review.setIsPositive(isPositive);
        review.setUserId(userId);
        review.setFilmId(filmId);
        review.setUseful(0);
        return review;
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