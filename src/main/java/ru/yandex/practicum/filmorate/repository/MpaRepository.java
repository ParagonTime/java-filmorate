package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.MpaDto;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaRepository extends BaseRepository<MpaDto> {
    private static final String FIND_BY_ID = "SELECT * FROM ratings WHERE id = ?";
    private static final String FIND_ALL_MPA = "SELECT * FROM ratings";
    private static final String FIND_FILM_MPA =
            "SELECT r.id, r.rating_name FROM films AS f JOIN ratings AS r ON f.rating_id = r.id WHERE f.id = ?";

    public MpaRepository(JdbcTemplate jdbc, RowMapper<MpaDto> mapper) {
        super(jdbc, mapper);
    }

    public Optional<MpaDto> getMpaById(long id) {
        return findOne(FIND_BY_ID, id);
    }

    public List<MpaDto> getAllMpa() {
        return findMany(FIND_ALL_MPA);
    }

    public Optional<MpaDto> getMpaByFilmId(long filmId) {
        return findOne(FIND_FILM_MPA, filmId);
    }
}
