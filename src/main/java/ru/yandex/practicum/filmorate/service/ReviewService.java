package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FeedMapper;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.FeedEventType;
import ru.yandex.practicum.filmorate.model.FeedOperationType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.FeedRepository;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.ReviewRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final ReviewMapper reviewMapper;
    private final FeedRepository feedRepository;

    @Transactional
    public ReviewDto postReview(NewReviewRequest request) {
        log.debug("create review {}", request);
        validateUserAndFilm(request.getUserId(), request.getFilmId());

        Review review = reviewMapper.mapToReview(request);
        review = reviewRepository.save(review);

        Feed feed = new Feed();
        feed.setUserId(review.getUserId());
        feed.setTimestamp(System.currentTimeMillis());
        feed.setEventType(FeedEventType.REVIEW);
        feed.setOperation(FeedOperationType.ADD);
        feed.setEntityId(review.getReviewId());
        feedRepository.addEvent(feed);

        return reviewMapper.mapToReviewDto(review);
    }

    @Transactional
    public ReviewDto putReview(UpdateReviewRequest request) {
        log.debug("update review {}", request);
        validateUserAndFilm(request.getUserId(), request.getFilmId());

        Review review = reviewRepository.getReview(request.getReviewId());
        Review updatedReview = reviewMapper.updateReviewFields(review, request);
        updatedReview = reviewRepository.update(updatedReview);

        Feed feed = new Feed();
        feed.setUserId(updatedReview.getUserId());
        feed.setTimestamp(System.currentTimeMillis());
        feed.setEventType(FeedEventType.REVIEW);
        feed.setOperation(FeedOperationType.UPDATE);
        feed.setEntityId(updatedReview.getReviewId());
        feedRepository.addEvent(feed);

        return reviewMapper.mapToReviewDto(updatedReview);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        log.debug("delete review {}", reviewId);
        Review review4Delete = reviewRepository.getReview(reviewId);

        Feed feed = new Feed();
        feed.setUserId(review4Delete.getUserId());
        feed.setTimestamp(System.currentTimeMillis());
        feed.setEventType(FeedEventType.REVIEW);
        feed.setOperation(FeedOperationType.REMOVE);
        feed.setEntityId(review4Delete.getReviewId());
        feedRepository.addEvent(feed);

        reviewRepository.deleteById(reviewId);
    }

    public ReviewDto getReview(Long reviewId) {
        log.debug("get review {}", reviewId);
        Review review = reviewRepository.getReview(reviewId);
        return reviewMapper.mapToReviewDto(review);
    }

    public Collection<ReviewDto> getReviews(Long filmId, Integer count) {
        log.debug("get reviews by filmId {} count {}", filmId, count);

        int reviewCount = (count == null) ? 10 : count;
        if (reviewCount <= 0) {
            throw new ValidationException("Параметр count должен быть положительным");
        }

        if (filmId != null) {
            filmRepository.getFilm(filmId);
        }

        return reviewRepository.getReviews(filmId, reviewCount).stream()
                .map(reviewMapper::mapToReviewDto)
                .toList();
    }

    @Transactional
    public void addLike(Long reviewId, Long userId) {
        log.debug("add like review {} by user {}", reviewId, userId);
        validateReviewAndUser(reviewId, userId);
        reviewRepository.addReaction(reviewId, userId, 1);
    }

    @Transactional
    public void addDislike(Long reviewId, Long userId) {
        log.debug("add dislike review {} by user {}", reviewId, userId);
        validateReviewAndUser(reviewId, userId);
        reviewRepository.addReaction(reviewId, userId, -1);
    }

    @Transactional
    public void deleteLike(Long reviewId, Long userId) {
        log.debug("delete like review {} by user {}", reviewId, userId);
        validateReviewAndUser(reviewId, userId);
        reviewRepository.deleteReaction(reviewId, userId, 1);
    }

    @Transactional
    public void deleteDislike(Long reviewId, Long userId) {
        log.debug("delete dislike review {} by user {}", reviewId, userId);
        validateReviewAndUser(reviewId, userId);
        reviewRepository.deleteReaction(reviewId, userId, -1);
    }

    private void validateUserAndFilm(Long userId, Long filmId) {
        userRepository.getUser(userId);
        filmRepository.getFilm(filmId);
    }

    private void validateReviewAndUser(Long reviewId, Long userId) {
        reviewRepository.getReview(reviewId);
        userRepository.getUser(userId);
    }
}