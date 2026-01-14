package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final LocalDate START_RELEASE_FILMS = LocalDate.of(1895, 12, 28);
    private static final String NO_EMPTY_ID_EXCEPTION_MESSAGE = "ID не может быть пустым";
    private static final String NO_FOUND_FILM_EXCEPTION_MESSAGE = "Фильм с указанным ID не найден";
    private static final String NO_BLANK_EXCEPTION_MESSAGE = "Название не может быть пустым";
    private static final String DATE_RELEASE_EXCEPTION_MESSAGE = "Дата релиза — не раньше 28 декабря 1895 года";
    private static final String FILM_EXIST_EXCEPTION_MESSAGE = "Фильм уже в базе";

    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film postFilms(@Valid @RequestBody Film film) {
        validateFilm(film);
        film.setId(nextId());
        films.put(film.getId(), film);
        log.debug("New film: {}", film);
        return film;
    }

    @PutMapping
    public Film putFilms(@Valid @RequestBody Film newFilm) {

        if (newFilm == null || newFilm.getId() == null) {
            log.warn(NO_EMPTY_ID_EXCEPTION_MESSAGE);
            throw new ValidationException(NO_EMPTY_ID_EXCEPTION_MESSAGE);
        }
        Film currentFilm = films.get(newFilm.getId());
        if (currentFilm == null) {
            log.warn(NO_FOUND_FILM_EXCEPTION_MESSAGE);
            throw new NotFoundException(NO_FOUND_FILM_EXCEPTION_MESSAGE);
        }
        if (newFilm.getName() != null) {
            currentFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null) {
            currentFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            currentFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null) {
            currentFilm.setDuration(newFilm.getDuration());
        }
        log.debug("Put film: {}", currentFilm);
        return currentFilm;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        System.out.println(films.size());
        return films.values();
    }

    private void validateFilm(Film film) {
        try {
            if (film == null || film.getName().isBlank()) {
                throw new ValidationException(NO_BLANK_EXCEPTION_MESSAGE);
            }
            if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(START_RELEASE_FILMS)) {
                throw new ValidationException(DATE_RELEASE_EXCEPTION_MESSAGE);
            }
            if (isFilmExist(film.getName(), film.getReleaseDate())) {
                throw new DuplicatedDataException(FILM_EXIST_EXCEPTION_MESSAGE);
            }
        } catch (RuntimeException e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    private boolean isFilmExist(String name, LocalDate date) {
        return !films.values().stream()
                .filter(film -> film.getName().equals(name) && film.getReleaseDate().equals(date))
                .toList()
                .isEmpty();
    }

    private long nextId() {
        return films.size() + 1;
    }
}
