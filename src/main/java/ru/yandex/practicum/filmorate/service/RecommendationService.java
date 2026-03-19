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

        Collection<Film> allFilms = filmRepository.getFilms();

        Set<Long> userLikedFilmIds = getUserLikedFilmIds(userId, allFilms);

        log.debug("user {} liked {} films", userId, userLikedFilmIds.size());

        if (userLikedFilmIds.isEmpty()) {
            log.debug("user {} has no likes, no recommendations", userId);
            return Collections.emptyList();
        }

        Map<Long, Set<Long>> otherUsersLikes = getOtherUsersLikes(userId, allFilms);

        Long similarUser = findUserWithMaxIntersection(userLikedFilmIds, otherUsersLikes);

        if (similarUser == null) {
            log.debug("No similar user found for user {}", userId);
            return Collections.emptyList();
        }

        log.debug("found similar user {} for user {}", similarUser, userId);

        Set<Long> recommendations = getRecommendationsFromUser(
                similarUser, userLikedFilmIds, otherUsersLikes);

        log.debug("found {} recommendations for user {} from similar user {}",
                recommendations.size(), userId, similarUser);

        return recommendations.stream()
                .map(this::getFilmDtoWithDetails)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Set<Long> getUserLikedFilmIds(Long userId, Collection<Film> allFilms) {
        return allFilms.stream()
                .filter(film -> film.getUsersLiked().contains(userId))
                .map(Film::getId)
                .collect(Collectors.toSet());
    }

    private Map<Long, Set<Long>> getOtherUsersLikes(Long currentUserId, Collection<Film> allFilms) {
        Map<Long, Set<Long>> usersLikes = new HashMap<>();

        for (Film film : allFilms) {
            for (Long userId : film.getUsersLiked()) {
                if (!userId.equals(currentUserId)) {
                    usersLikes.computeIfAbsent(userId, k -> new HashSet<>())
                            .add(film.getId());
                }
            }
        }

        return usersLikes;
    }

    private Long findUserWithMaxIntersection(Set<Long> targetUserLikes,
                                             Map<Long, Set<Long>> otherUsersLikes) {
        Map<Long, Integer> intersections = new HashMap<>();

        for (Map.Entry<Long, Set<Long>> entry : otherUsersLikes.entrySet()) {
            Long userId = entry.getKey();
            Set<Long> userLikes = entry.getValue();

            Set<Long> intersection = new HashSet<>(targetUserLikes);
            intersection.retainAll(userLikes);

            if (!intersection.isEmpty()) {
                intersections.put(userId, intersection.size());
            }
        }

        return intersections.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private Set<Long> getRecommendationsFromUser(Long similarUserId,
                                                 Set<Long> targetUserLikes,
                                                 Map<Long, Set<Long>> otherUsersLikes) {
        Set<Long> similarUserLikes = otherUsersLikes.get(similarUserId);
        if (similarUserLikes == null) {
            return Collections.emptySet();
        }

        Set<Long> recommendations = new HashSet<>(similarUserLikes);
        recommendations.removeAll(targetUserLikes);

        return recommendations;
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