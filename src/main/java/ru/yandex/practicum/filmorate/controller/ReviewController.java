package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto postReview(@Valid @RequestBody NewReviewRequest request) {
        return reviewService.postReview(request);
    }

    @PutMapping
    public ReviewDto putReview(@Valid @RequestBody UpdateReviewRequest request) {
        return reviewService.putReview(request);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable("id") Long reviewId) {
        reviewService.deleteReview(reviewId);
    }

    @GetMapping("/{id}")
    public ReviewDto getReview(@PathVariable("id") Long reviewId) {
        return reviewService.getReview(reviewId);
    }

    @GetMapping
    public Collection<ReviewDto> getReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") Integer count
    ) {
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") Long reviewId,
                        @PathVariable("userId") Long userId) {
        reviewService.addLike(reviewId, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable("id") Long reviewId,
                           @PathVariable("userId") Long userId) {
        reviewService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") Long reviewId,
                           @PathVariable("userId") Long userId) {
        reviewService.deleteLike(reviewId, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable("id") Long reviewId,
                              @PathVariable("userId") Long userId) {
        reviewService.deleteDislike(reviewId, userId);
    }
}