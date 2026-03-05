package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private static final LocalDate START_RELEASE_FILMS = LocalDate.of(1895, 12, 28);
    private static final String NO_EMPTY_ID_EXCEPTION_MESSAGE = "ID не может быть пустым";
    private static final String NO_FOUND_FILM_EXCEPTION_MESSAGE = "Фильм с указанным ID не найден";
    private static final String NO_BLANK_EXCEPTION_MESSAGE = "Название не может быть пустым";
    private static final String DATE_RELEASE_EXCEPTION_MESSAGE = "Дата релиза — не раньше 28 декабря 1895 года";
    private static final String FILM_EXIST_EXCEPTION_MESSAGE = "Фильм уже в базе";

    private final UserService userService;
    private final Map<Long, Film> films;

    public InMemoryFilmStorage(UserService userService) {
        this.userService = userService;
        this.films = new HashMap<>();
    }

    @Override
    public Film postFilm(Film film) {
        validateFilm(film);
        film.setId(nextId());
        films.put(film.getId(), film);
        log.debug("New film: {}", film);
        return film;
    }

    @Override
    public Film putFilm(Film newFilm) {

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

    @Override
    public Collection<Film> getFilms() {
        log.debug("Call getFilms with {}", films.values());
        return films.values();
    }

    @Override
    public Film getFilm(Long id) {
        if (films.containsKey(id)) {
            return films.get(id);
        }
        throw new NotFoundException(NO_FOUND_FILM_EXCEPTION_MESSAGE);
    }

    @Override
    public Boolean addLike(Long filmId, Long userId) {
        if (films.containsKey(filmId) && userService.userExist(userId)) {
            return films.get(filmId).getUsersLiked().add(userId);
        }
        throw new NotFoundException(NO_FOUND_FILM_EXCEPTION_MESSAGE);
    }

    @Override
    public Boolean deleteLike(Long filmId, Long userId) {
        if (films.containsKey(filmId) && userService.userExist(userId)) {
            return films.get(filmId).getUsersLiked().remove(userId);
        }
        throw new NotFoundException(NO_FOUND_FILM_EXCEPTION_MESSAGE);
    }

    @Override
    public Collection<Film> getPopularFilms(Long count) {
        return films.values().stream()
                .sorted((o1, o2) -> Integer.compare(o2.getUsersLiked().size(), o1.getUsersLiked().size()))
                .limit(count)
                .toList();
    }

    private void validateFilm(Film film) {
        if (film == null || film.getName().isBlank()) {
            log.warn(NO_BLANK_EXCEPTION_MESSAGE);
            throw new ValidationException(NO_BLANK_EXCEPTION_MESSAGE);
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(START_RELEASE_FILMS)) {
            log.warn(DATE_RELEASE_EXCEPTION_MESSAGE);
            throw new ValidationException(DATE_RELEASE_EXCEPTION_MESSAGE);
        }
        if (isFilmExist(film.getName(), film.getReleaseDate())) {
            log.warn(FILM_EXIST_EXCEPTION_MESSAGE);
            throw new DuplicatedDataException(FILM_EXIST_EXCEPTION_MESSAGE);
        }
    }

    private boolean isFilmExist(String name, LocalDate date) {
        return films.values().stream()
                .anyMatch(film -> film.getName().equals(name) && film.getReleaseDate().equals(date));
    }

    private long nextId() {
        return films.size() + 1;
    }
}
