package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;

    public Film postFilm(@Valid Film film) {
        return filmStorage.postFilm(film);
    }

    public Film putFilm(@Valid Film newFilm) {
        return filmStorage.putFilm(newFilm);
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilm(Long id) {
        return filmStorage.getFilm(id);
    }

    public Boolean addLine(Long filmId, Long userId) {
        return filmStorage.addLike(filmId, userId);
    }

    public Boolean deleteLine(Long filmId, Long userId) {
        return filmStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> getPopularFilms(Long count) {
        return filmStorage.getPopularFilms(count);
    }
}
