package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film postFilm(Film film);

    Film putFilm(Film newFilm);

    Collection<Film> getFilms();

    Film getFilm(Long id);

    Boolean addLike(Long filmId, Long userId);

    Boolean deleteLike(Long filmId, Long userId);

    Collection<Film> getPopularFilms(Long count);
}
