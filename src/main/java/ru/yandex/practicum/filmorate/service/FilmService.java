package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.GenreRepository;
import ru.yandex.practicum.filmorate.repository.MpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final LocalDate START_RELEASE_FILMS = LocalDate.of(1895, 12, 28);
    private final FilmRepository filmRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;

    @Transactional
    public FilmDto postFilm(NewFilmRequest request) {
        log.debug("create film: {}", request);
        if (request.getReleaseDate() == null || request.getReleaseDate().isBefore(START_RELEASE_FILMS)) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895");
        }
        int genreCount = genreRepository.getAllGenres().size();
        if (request.getGenres() != null && request.getGenres().stream().anyMatch(genre -> genre.getId() > genreCount)) {
            throw new NotFoundException("Выбран несуществующий жанр");
        }
        int mpaCount = mpaRepository.getAllMpa().size();
        if (request.getMpa() != null && request.getMpa().getId() > mpaCount) {
            throw new NotFoundException("Выбран несуществующий рейтинг");
        }
        Film film = FilmMapper.mapToFilm(request);
        Film savedFilm = filmRepository.save(film);
        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            request.getGenres().stream()
                    .map(GenreDto::getId)
                    .distinct()
                    .forEach(genreId -> filmRepository.saveGenres(savedFilm.getId(), genreId)
            );
        }
        return getFilmDto(savedFilm);
    }

    @Transactional
    public FilmDto putFilm(UpdateFilmRequest request) {
        log.debug("update film: {}", request);
        if (request.hasReleaseDate() && request.getReleaseDate().isBefore(START_RELEASE_FILMS)) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895");
        }
        int genreCount = genreRepository.getAllGenres().size();
        if (request.hasGenres() && request.getGenres().stream().anyMatch(genre -> genre.getId() > genreCount)) {
            throw new NotFoundException("Выбран несуществующий жанр");
        }
        int mpaCount = mpaRepository.getAllMpa().size();
        if (request.hasMpa() && request.getMpa().getId() > mpaCount) {
            throw new NotFoundException("Выбран несуществующий рейтинг");
        }
        Film film = filmRepository.getFilm(request.getId());
        Film updatedFilm = FilmMapper.updateFilmFields(film, request);
        Film savedFilm = filmRepository.update(updatedFilm);
        if (request.hasGenres()) {
            filmRepository.deleteGenres(savedFilm.getId());
            request.getGenres().stream()
                    .map(GenreDto::getId)
                    .distinct()
                    .forEach(genreId -> filmRepository.saveGenres(savedFilm.getId(), genreId)
                    );
        }
        return getFilmDto(savedFilm);
    }

    public Collection<FilmDto> getFilms() {
        log.debug("get films");
        return filmRepository.getFilms().stream()
                .map(this::getFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto getFilm(Long id) {
        log.debug("get film by id: {}", id);
        Film film = filmRepository.getFilm(id);
        return getFilmDto(film);
    }

    @Transactional
    public Boolean addLike(Long filmId, Long userId) {
        log.debug("add like film {} by user {}", filmId, userId);
        return filmRepository.addLike(filmId, userId);
    }

    @Transactional
    public Boolean deleteLike(Long filmId, Long userId) {
        log.debug("delete like film {} by user {}", filmId, userId);
        return filmRepository.deleteLike(filmId, userId);
    }

    public Collection<FilmDto> getPopularFilms(Long count) {
        log.debug("get popular films: {}", count);
        return filmRepository.getPopularFilms(count).stream()
                .map(this::getFilmDto)
                .collect(Collectors.toList());
    }

    private FilmDto getFilmDto(Film film) {
        MpaDto mpa = mpaRepository.getMpaById(film.getRatingId()).orElse(null);
        List<GenreDto> genres = genreRepository.getAllGenresByFilmId(film.getId());
        return FilmMapper.mapToFilmDto(film, mpa, genres);
    }
}
