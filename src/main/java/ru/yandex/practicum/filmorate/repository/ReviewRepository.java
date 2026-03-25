package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

@Repository
public class ReviewRepository extends BaseRepository<Review> {

    private static final String INSERT_QUERY = """
            INSERT INTO reviews(content, is_positive, user_id, film_id, useful)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_QUERY = """
            UPDATE reviews
            SET content = ?, is_positive = ?
            WHERE review_id = ?
            """;

    private static final String DELETE_QUERY = """
            DELETE FROM reviews
            WHERE review_id = ?
            """;

    private static final String FIND_REVIEW_BY_ID = """
            SELECT *
            FROM reviews
            WHERE review_id = ?
            """;

    private static final String FIND_ALL_REVIEWS = """
            SELECT *
            FROM reviews
            ORDER BY useful DESC
            LIMIT ?
            """;

    private static final String FIND_REVIEWS_BY_FILM_ID = """
            SELECT *
            FROM reviews
            WHERE film_id = ?
            ORDER BY useful DESC
            LIMIT ?
            """;

    private static final String UPSERT_REACTION = """
            MERGE INTO review_reaction (review_id, user_id, reaction)
            KEY (review_id, user_id)
            VALUES (?, ?, ?)
            """;

    private static final String DELETE_REACTION = """
            DELETE FROM review_reaction
            WHERE review_id = ? AND user_id = ? AND reaction = ?
            """;

    private static final String UPDATE_USEFUL = """
            UPDATE reviews
            SET useful = COALESCE((
                SELECT SUM(reaction)
                FROM review_reaction
                WHERE review_id = ?
            ), 0)
            WHERE review_id = ?
            """;

    public ReviewRepository(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    public Review save(Review review) {
        Long id = insert(
                INSERT_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful()
        );
        review.setReviewId(id);
        return review;
    }

    public Review update(Review review) {
        update(
                UPDATE_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );
        return review;
    }

    public boolean deleteById(Long reviewId) {
        return delete(DELETE_QUERY, reviewId);
    }

    public Review getReview(Long reviewId) {
        return findOne(FIND_REVIEW_BY_ID, reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + reviewId + " не найден"));
    }

    public Collection<Review> getReviews(Long filmId, int count) {
        if (filmId == null) {
            return findMany(FIND_ALL_REVIEWS, count);
        }
        return findMany(FIND_REVIEWS_BY_FILM_ID, filmId, count);
    }

    public boolean addReaction(Long reviewId, Long userId, int reaction) {
        boolean updated = update(UPSERT_REACTION, reviewId, userId, reaction) > 0;
        recalculateUseful(reviewId);
        return updated;
    }

    public boolean deleteReaction(Long reviewId, Long userId, int reaction) {
        boolean updated = update(DELETE_REACTION, reviewId, userId, reaction) > 0;
        recalculateUseful(reviewId);
        return updated;
    }

    private void recalculateUseful(Long reviewId) {
        update(UPDATE_USEFUL, reviewId, reviewId);
    }
}