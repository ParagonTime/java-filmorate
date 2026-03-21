package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

        Set<Long> userLikedFilmIds = getLikedFilmIds(userId);

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

    private Set<Long> getLikedFilmIds(Long userId) {
        Collection<Film> films = filmRepository.getFilms();
        Set<Long> likedFilmIds = new HashSet<>();

        for (Film film : films) {
            if (isFilmLikedByUser(film.getId(), userId)) {
                likedFilmIds.add(film.getId());
            }
        }

        return likedFilmIds;
    }

    private boolean isFilmLikedByUser(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM user_like WHERE film_id = ? AND user_id = ?";
        Integer count = filmRepository.getJdbc().queryForObject(sql, Integer.class, filmId, userId);
        return count != null && count > 0;
    }

    private Long findSimilarUser(Long currentUserId, Set<Long> userLikedFilmIds) {
        String sql = "SELECT user_id, COUNT(*) as common_likes " +
                "FROM user_like " +
                "WHERE user_id != ? AND film_id IN (" +
                userLikedFilmIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ") " +
                "GROUP BY user_id " +
                "ORDER BY common_likes DESC " +
                "LIMIT 1";

        List<Object> params = new ArrayList<>();
        params.add(currentUserId);
        params.addAll(userLikedFilmIds);

        try {
            return filmRepository.getJdbc().queryForObject(sql, (rs, rowNum) -> rs.getLong("user_id"), params.toArray());
        } catch (Exception e) {
            log.debug("No similar user found: {}", e.getMessage());
            return null;
        }
    }

    private Set<Long> getRecommendationsFromUser(Long similarUserId, Set<Long> userLikedFilmIds) {
        String sql = "SELECT film_id FROM user_like WHERE user_id = ? AND film_id NOT IN (" +
                userLikedFilmIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";

        List<Object> params = new ArrayList<>();
        params.add(similarUserId);
        params.addAll(userLikedFilmIds);

        try {
            List<Long> recommendedIds = filmRepository.getJdbc().queryForList(sql, Long.class, params.toArray());
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
            return filmMapper.mapToFilmDto(film, mpa, genres);
        } catch (NotFoundException e) {
            log.warn("film with id {} not found", filmId);
            return null;
        }
    }
}