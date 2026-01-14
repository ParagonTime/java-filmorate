package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    @Test
    void shouldCreateFilmSuccessfully() {
        Film film = new Film();
        film.setName("Звездные войны");
        film.setDescription("Давным-давно в далекой галактике...");
        film.setReleaseDate(LocalDate.of(1977, 5, 25));
        film.setDuration(121);

        Film savedFilm = filmController.postFilms(film);

        assertNotNull(savedFilm.getId());
        assertEquals("Звездные войны", savedFilm.getName());
        assertEquals(1, filmController.getFilms().size());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        Film film = new Film();
        film.setName("  ");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        assertThrows(ValidationException.class, () -> filmController.postFilms(film));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsTooLong() {
        Film film = new Film();
        film.setName("Фильм с длинным описанием");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        assertThrows(ValidationException.class, () -> filmController.postFilms(film));
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsTooEarly() {
        Film film = new Film();
        film.setName("Старое кино");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(100);

        assertThrows(ValidationException.class, () -> filmController.postFilms(film));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Назад в прошлое");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> filmController.postFilms(film));
    }

    @Test
    void shouldThrowExceptionWhenFilmAlreadyExists() {
        Film film = new Film();
        film.setName("Дубликат");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);

        filmController.postFilms(film);

        Film duplicateFilm = new Film();
        duplicateFilm.setName("Дубликат");
        duplicateFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        duplicateFilm.setDuration(120);

        assertThrows(DuplicatedDataException.class, () -> filmController.postFilms(duplicateFilm));
    }

    @Test
    void shouldUpdateFilmSuccessfully() {
        Film film = new Film();
        film.setName("Оригинал");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        filmController.postFilms(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(1L);
        updatedFilm.setName("Обновленное название");
        updatedFilm.setDuration(150);

        filmController.putFilms(updatedFilm);

        Film result = filmController.getFilms().iterator().next();
        assertEquals("Обновленное название", result.getName());
        assertEquals(150, result.getDuration());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistentFilm() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Призрак");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));

        assertThrows(NotFoundException.class, () -> filmController.putFilms(film));
    }
}
