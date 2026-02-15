package ru.yandex.practicum.filmorate.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorResponse {
    private final String error;

    public String getMessage() {
        return error;
    }
}
