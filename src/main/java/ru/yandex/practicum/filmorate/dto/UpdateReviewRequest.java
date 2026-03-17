package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateReviewRequest {
    @NotNull(message = "Id отзыва не может быть null")
    private Long reviewId;

    @NotBlank(message = "Текст отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Поле isPositive не может быть null")
    private Boolean isPositive;

    @NotNull(message = "Id пользователя не может быть null")
    private Long userId;

    @NotNull(message = "Id фильма не может быть null")
    private Long filmId;
}