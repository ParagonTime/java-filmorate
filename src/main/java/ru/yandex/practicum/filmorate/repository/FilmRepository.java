package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@Repository
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {

    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
    private static final String FIND_ALL_FILMS = "SELECT * FROM films";
    private static final String FIND_FILM_BY_ID = "SELECT * FROM films WHERE id = ?";
    private static final String INSERT_LIKE = "INSERT INTO user_like(film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM user_like WHERE film_id = ? AND user_id = ?";
    private static final String FIND_POPULAR_FILMS =
            "SELECT f.*, COUNT(ul.user_id) as likes_count " +
                    "FROM films f LEFT JOIN user_like ul ON f.id = ul.film_id " +
                    "GROUP BY f.id ORDER BY likes_count DESC LIMIT ?";
    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genre WHERE film_id = ?";

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Film save(Film film) {
        Long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRatingId()
        );
        film.setId(id);
        return film;
    }

    @Override
    public Film update(Film film) {
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRatingId(),
                film.getId()
        );
        return film;
    }

    @Override
    public Film getFilm(Long id) {
        return findOne(FIND_FILM_BY_ID, id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    @Override
    public Collection<Film> getFilms() {
        return findMany(FIND_ALL_FILMS);
    }

    @Override
    public Boolean addLike(Long filmId, Long userId) {
        return update(INSERT_LIKE, filmId, userId) > 0;
    }

    @Override
    public Boolean deleteLike(Long filmId, Long userId) {
        return update(DELETE_LIKE, filmId, userId) > 0;
    }

    @Override
    public Collection<Film> getPopularFilms(Long count) {
        return findMany(FIND_POPULAR_FILMS, count);
    }

    public void saveGenres(Long filmId, Long genreId) {
        update(INSERT_FILM_GENRE, filmId, genreId);
    }

    public void deleteGenres(Long filmId) {
        update(DELETE_FILM_GENRES, filmId);
    }
}

