package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film postFilms(@RequestBody Film film) {

        return null;
    }

    @PutMapping
    public Film putFilms(@RequestBody Film newFilm) {

        return null;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }
}
