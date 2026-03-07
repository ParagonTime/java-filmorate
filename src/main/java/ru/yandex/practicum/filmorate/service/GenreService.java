package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.GenreRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public Collection<GenreDto> getAllGenres() {
        return genreRepository.getAllGenres();
    }

    public GenreDto getGenreById(Long id) {
        return genreRepository.getGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с таким id не существует"));
    }

    public Collection<GenreDto> getGenreByFilmId(long id) {
        return genreRepository.getAllGenresByFilmId(id);
    }
}
