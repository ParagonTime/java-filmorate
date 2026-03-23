package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.FeedEventType;
import ru.yandex.practicum.filmorate.model.FeedOperationType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final LocalDate START_RELEASE_FILMS = LocalDate.of(1895, 12, 28);
    private final FilmRepository filmRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final DirectorRepository directorRepository;
    private final FilmMapper filmMapper;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    private static final String NO_NEGATIVE_PARAMETER_MESSAGE = "Парамерты не могут быть меньше 0";

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
        Film film = filmMapper.mapToFilm(request);
        Film savedFilm = filmRepository.save(film);
        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            request.getGenres().stream()
                    .map(GenreDto::getId)
                    .distinct()
                    .forEach(genreId -> filmRepository.saveGenres(savedFilm.getId(), genreId)
                    );
        }
        if (request.getDirectors() != null && !request.getDirectors().isEmpty()) {
            request.getDirectors().stream()
                    .map(DirectorDto::getId)
                    .distinct()
                    .forEach(directorId -> directorRepository.saveDirectorForFilm(savedFilm.getId(), directorId));
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
        Film updatedFilm = filmMapper.updateFilmFields(film, request);
        Film savedFilm = filmRepository.update(updatedFilm);
        if (request.hasGenres()) {
            filmRepository.deleteGenres(savedFilm.getId());
            request.getGenres().stream()
                    .map(GenreDto::getId)
                    .distinct()
                    .forEach(genreId -> filmRepository.saveGenres(savedFilm.getId(), genreId));
        }
        if (request.hasDirector()) {
            directorRepository.deleteDirectors(savedFilm.getId());
            request.getDirectors().stream()
                    .map(DirectorDto::getId)
                    .distinct()
                    .forEach(directorId -> directorRepository.saveDirectorForFilm(savedFilm.getId(), directorId));
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
        if (id < 0) {
            throw new ValidationException(NO_NEGATIVE_PARAMETER_MESSAGE);
        }
        Film film = filmRepository.getFilm(id);
        return getFilmDto(film);
    }

    @Transactional
    public Boolean addLike(Long filmId, Long userId) {
        log.debug("add like film {} by user {}", filmId, userId);
        filmRepository.getFilm(filmId);
        userRepository.getUser(userId);
        Boolean result = filmRepository.addLike(filmId, userId);
        feedRepository.addEventByParams(userId, System.currentTimeMillis(), FeedEventType.LIKE, FeedOperationType.ADD, filmId);
        return result;
    }

    @Transactional
    public Boolean deleteLike(Long filmId, Long userId) {
        log.debug("delete like film {} by user {}", filmId, userId);
        filmRepository.getFilm(filmId);
        userRepository.getUser(userId);
        Boolean result = filmRepository.deleteLike(filmId, userId);
        feedRepository.addEventByParams(userId, System.currentTimeMillis(), FeedEventType.LIKE, FeedOperationType.REMOVE, filmId);
        return result;
    }

    public Collection<FilmDto> getPopularFilms(Long count) {
        if (count < 0) {
            throw new ValidationException(NO_NEGATIVE_PARAMETER_MESSAGE);
        }
        return filmRepository.getPopularFilms(count).stream()
                .map(this::getFilmDto)
                .collect(Collectors.toList());
    }

    private FilmDto getFilmDto(Film film) {
        MpaDto mpa = mpaRepository.getMpaById(film.getRatingId()).orElse(null);
        List<GenreDto> genres = genreRepository.getAllGenresByFilmId(film.getId());
        List<DirectorDto> directorDtos = directorRepository.getDirectorsByFilm(film.getId());
        return filmMapper.mapToFilmDto(film, mpa, genres, directorDtos);
    }

    public Collection<FilmDto> getFilmsByDirector(Long directorId, String sortBy) {
        if (!List.of("year", "likes").contains(sortBy)) {
            throw new ValidationException("Неизвестный аргумент sortBy: " + sortBy);
        }
        directorRepository.getDirector(directorId);
        return filmRepository.getFilmsByDirector(directorId, sortBy).stream()
                .map(this::getFilmDto)
                .collect(Collectors.toList());
    }

    public Collection<FilmDto> getPopularWithGenreByYear(Integer limit, Long genreId, Integer year) {
        log.debug("get popular films: genre={} year={} count={}", genreId, year, limit);
        return filmRepository.getFilmsWithGenreByYear(limit, genreId, year).stream()
                .map(this::getFilmDto)
                .toList();
    }

    public Collection<FilmDto> getCommonFilms(Long userId, Long friendId) {
        log.debug("get common films for users {} and {}", userId, friendId);

        userRepository.getUser(userId);
        userRepository.getUser(friendId);

        return filmRepository.getCommonFilms(userId, friendId).stream()
                .map(this::getFilmDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFilm(Long filmId) {
        log.debug("delete film with id {}", filmId);

        filmRepository.deleteGenres(filmId);

        directorRepository.deleteDirectors(filmId);

        boolean deleted = filmRepository.deleteFilm(filmId);

        if (!deleted) {
            throw new NotFoundException("Не удалось удалить фильм с id " + filmId);
        }
    }

    public Collection<FilmDto> getSearchFilms(Map<String, String> searchParams) {
        log.debug("search films by params: {}", searchParams.toString());
        if (searchParams.size()  != 2) {
            throw new ValidationException("Не указаны или неверно указаны параметры поиска. Корректный пример: ?query=крад&by=director,title");
        }
        if (!searchParams.containsKey("query")) {
            throw new ValidationException("Среди параметров нет ключа 'query'. Непонятно, что искать");
        }
        String query = searchParams.get("query");
        if (query == null || query.isBlank()) {
            throw new ValidationException("Неправильно указано значение параметра 'query'. Непонятно, что искать");
        }
        if (!searchParams.containsKey("by")) {
            throw new ValidationException("Среди параметров нет ключа 'by'. Непонятно, где искать");
        }
        String by = searchParams.get("by");
        if (by == null || by.isBlank() ||
                ((!by.trim().toLowerCase().contains("director")) && (!by.trim().toLowerCase().contains("title")))) {
            throw new ValidationException("Неправильно указано значение параметра 'by'. Непонятно, где искать");
        }
        Boolean searchByDirector = by.trim().toLowerCase().contains("director");
        Boolean searchByTitle = by.trim().toLowerCase().contains("title");

        return filmRepository.getSearchFilms(query, searchByDirector, searchByTitle).stream()
                .map(this::getFilmDto)
                .collect(Collectors.toList());
    }
}