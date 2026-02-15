package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film postFilm(@Valid @RequestBody Film film) {
        return filmService.postFilm(film);
    }

    @PutMapping
    public Film putFilm(@Valid @RequestBody Film newFilm) {
        return filmService.putFilm(newFilm);
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getUser(@PathVariable("id") Long id) {
        return filmService.getFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Boolean addLike(@PathVariable("id") Long filmId, @PathVariable("userId") Long userId) {
        return filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Boolean deleteLike(@PathVariable("id") Long filmId, @PathVariable("userId") Long userId) {
        return filmService.deleteLike(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") Long count) {
        return filmService.getPopularFilms(count);
    }
}
