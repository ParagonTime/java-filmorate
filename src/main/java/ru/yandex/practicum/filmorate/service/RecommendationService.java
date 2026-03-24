package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.GenreRepository;
import ru.yandex.practicum.filmorate.repository.MpaRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final FilmMapper filmMapper;

    public Collection<FilmDto> getRecommendations(Long userId) {
        User user = userRepository.getUser(userId);

        Set<Long> userLikedFilmIds = filmRepository.getFilmsLikedByUser(userId).stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        log.debug("user {} liked {} films", userId, userLikedFilmIds.size());

        if (userLikedFilmIds.isEmpty()) {
            log.debug("user {} has no likes, no recommendations", userId);
            return Collections.emptyList();
        }

        Long similarUserId = findSimilarUser(userId, userLikedFilmIds);

        if (similarUserId == null) {
            log.debug("no similar user found for user {}", userId);
            return Collections.emptyList();
        }

        log.debug("found similar user {} for user {}", similarUserId, userId);

        Set<Long> recommendedFilmIds = getRecommendationsFromUser(similarUserId, userLikedFilmIds);

        log.debug("found {} recommendations for user {} from similar user {}",
                recommendedFilmIds.size(), userId, similarUserId);

        return recommendedFilmIds.stream()
                .map(this::getFilmDtoWithDetails)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Long findSimilarUser(Long currentUserId, Set<Long> userLikedFilmIds) {
        try {
            return filmRepository.findSimilarUser(currentUserId, userLikedFilmIds);
        } catch (Exception e) {
            log.debug("No similar user found: {}", e.getMessage());
            return null;
        }
    }

    private Set<Long> getRecommendationsFromUser(Long similarUserId, Set<Long> userLikedFilmIds) {
        try {
            List<Long> recommendedIds = filmRepository.getRecommendationsFromUser(similarUserId, userLikedFilmIds);
            return new HashSet<>(recommendedIds);
        } catch (Exception e) {
            log.debug("Error getting recommendations: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    private FilmDto getFilmDtoWithDetails(Long filmId) {
        try {
            Film film = filmRepository.getFilm(filmId);
            MpaDto mpa = mpaRepository.getMpaById(film.getRatingId()).orElse(null);
            List<GenreDto> genres = genreRepository.getAllGenresByFilmId(film.getId());
            List<DirectorDto> directors = Collections.emptyList();
            return filmMapper.mapToFilmDto(film, mpa, genres, directors);
        } catch (NotFoundException e) {
            log.warn("film with id {} not found", filmId);
            return null;
        }
    }
}