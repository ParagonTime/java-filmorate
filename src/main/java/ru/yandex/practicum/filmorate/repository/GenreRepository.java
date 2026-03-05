package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.GenreDto;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreRepository extends BaseRepository<GenreDto> {

    private static final String FIND_BY_ID = "SELECT * FROM genres WHERE id = ?";
    private static final String FIND_ALL_GENRES = "SELECT * FROM genres";
    private static final String FIND_ALL_GENRES_BY_ID =
            "SELECT g.* FROM film_genre fg JOIN genres g ON fg.genre_id = g.id WHERE fg.film_id = ?";

    public GenreRepository(JdbcTemplate jdbc, RowMapper<GenreDto> mapper) {
        super(jdbc, mapper);
    }

    public Optional<GenreDto> getGenreById(long id) {
        return findOne(FIND_BY_ID, id);
    }

    public List<GenreDto> getAllGenres() {
        return findMany(FIND_ALL_GENRES);
    }

    public List<GenreDto> getAllGenresByFilmId(long filmId) {
        return findMany(FIND_ALL_GENRES_BY_ID, filmId);
    }
}
