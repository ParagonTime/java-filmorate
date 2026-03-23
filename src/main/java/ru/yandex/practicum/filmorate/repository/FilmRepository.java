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
    private static final String FIND_DIRECTOR_FILMS_SORT_BY_LIKES =
            "SELECT f.*, COUNT(ul.user_id) as likes_count FROM films f " +
                    "LEFT JOIN user_like ul ON f.id = ul.film_id " +
                    "JOIN film_director fd ON f.id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.id " +
                    "ORDER BY likes_count DESC";
    private static final String FIND_DIRECTOR_FILMS_SORT_BY_YEAR =
            "SELECT f.* FROM films f " +
                    "JOIN film_director fd ON f.id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date ASC";
    private static final String FIND_COMMON_FILMS =
            "SELECT f.*, COUNT(ul_all.user_id) as likes_count " +
                    "FROM films f " +
                    "JOIN user_like ul1 ON f.id = ul1.film_id " +
                    "JOIN user_like ul2 ON f.id = ul2.film_id " +
                    "LEFT JOIN user_like ul_all ON f.id = ul_all.film_id " +
                    "WHERE ul1.user_id = ? AND ul2.user_id = ? " +
                    "GROUP BY f.id " +
                    "ORDER BY likes_count DESC";

    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE id = ?";

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

    public Collection<Film> getFilmsByDirector(Long directorId, String sortBy) {
        if (sortBy.equals("year")) {
            return findMany(FIND_DIRECTOR_FILMS_SORT_BY_YEAR, directorId);
        } else {
            return findMany(FIND_DIRECTOR_FILMS_SORT_BY_LIKES, directorId);
        }
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        return findMany(FIND_COMMON_FILMS, userId, friendId);
    }

    public boolean deleteFilm(Long filmId) {
        return delete(DELETE_FILM_QUERY, filmId);
    }
}
