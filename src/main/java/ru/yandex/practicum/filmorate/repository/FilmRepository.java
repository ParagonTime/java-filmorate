package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {

    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
    private static final String FIND_ALL_FILMS = "SELECT * FROM films";
    private static final String FIND_FILM_BY_ID = "SELECT * FROM films WHERE id = ?";
    private static final String INSERT_LIKE = "MERGE INTO user_like (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM user_like WHERE film_id = ? AND user_id = ?";
    private static final String FIND_POPULAR_FILMS =
            "SELECT f.*, COUNT(ul.user_id) as likes_count " +
                    "FROM films f LEFT JOIN user_like ul ON f.id = ul.film_id " +
                    "GROUP BY f.id ORDER BY likes_count DESC LIMIT ?";
    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String FIND_FILM_GENRE_YEAR =
            "SELECT f.*, COUNT(ul.user_id) as likes_count " +
                    "FROM films f " +
                    "JOIN film_genre fg ON f.id = fg.film_id " +
                    "LEFT JOIN user_like ul ON f.id = ul.film_id " +
                    "WHERE fg.genre_id = ? AND EXTRACT(YEAR FROM f.release_date) = ? " +
                    "GROUP BY f.id ORDER BY likes_count DESC";
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
    private static final String SEARCH_FILMS_QUERY = """
            SELECT f.*, d.name as director_name, count(ul.USER_ID) AS likes_count
            FROM films f
            LEFT JOIN FILM_DIRECTOR fd ON f.ID = fd.FILM_ID
            LEFT JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.ID
            LEFT JOIN USER_LIKE ul ON f.ID = ul.FILM_ID
            WHERE lower(trim(f.name)) LIKE lower(trim(?))
            	OR lower(trim(d.name)) LIKE lower(trim(?))
            GROUP BY f.id, fd.DIRECTOR_ID
            ORDER BY likes_count DESC, f.name, director_name
            """;
    private static final String LIMIT_ARG = " LIMIT ?";
    private static final String FIND_FILMS_LIKED_BY_USER =
            "SELECT f.* FROM films f JOIN user_like ul ON f.id = ul.film_id WHERE ul.user_id = ?";
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

    public Collection<Film> getFilmsWithGenreByYear(Integer limit, Long genreId, Integer year) {
        List<Object> params = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT f.*, COUNT(ul.user_id) as likes_count " +
                        "FROM films f " +
                        "LEFT JOIN user_like ul ON f.id = ul.film_id "
        );

        if (genreId != null) {
            queryBuilder.append("JOIN film_genre fg ON f.id = fg.film_id ");
        }

        List<String> conditions = new ArrayList<>();

        if (genreId != null) {
            conditions.add("fg.genre_id = ?");
            params.add(genreId);
        }

        if (year != null) {
            conditions.add("EXTRACT(YEAR FROM f.release_date) = ?");
            params.add(year);
        }

        if (!conditions.isEmpty()) {
            queryBuilder.append("WHERE ").append(String.join(" AND ", conditions));
        }

        queryBuilder.append(" GROUP BY f.id ORDER BY likes_count DESC ");

        if (limit != null) {
            queryBuilder.append("LIMIT ?");
            params.add(limit);
        }
        return findMany(queryBuilder.toString(), params.toArray());
    }

    public Collection<Film> getFilmsLikedByUser(Long userId) {
        return findMany(FIND_FILMS_LIKED_BY_USER, userId);
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        return findMany(FIND_COMMON_FILMS, userId, friendId);
    }

    public boolean deleteFilm(Long filmId) {
        return delete(DELETE_FILM_QUERY, filmId);
    }

    public Collection<Film> getSearchFilms(String query, Boolean searchByDirector, Boolean searchByTitle) {
        String parSearchByDirector = searchByDirector ? "%" + query.trim().toLowerCase() + "%" : "''";
        String parSearchByTitle = searchByTitle ? "%" + query.trim().toLowerCase() + "%" : "''";
        return findMany(SEARCH_FILMS_QUERY, parSearchByTitle, parSearchByDirector);
    }

    public List<Long> getRecommendationsFromUser(Long similarUserId, Set<Long> userLikedFilmIds) {
        String sql = "SELECT film_id FROM user_like WHERE user_id = ? AND film_id NOT IN (" +
                userLikedFilmIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";

        List<Object> params = new ArrayList<>();
        params.add(similarUserId);
        params.addAll(userLikedFilmIds);

        return jdbc.queryForList(sql, Long.class, params.toArray());
    }

    public Long findSimilarUser(Long currentUserId, Set<Long> userLikedFilmIds) {
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

        return jdbc.queryForObject(sql, (rs, rowNum) -> rs.getLong("user_id"), params.toArray());
    }
}
