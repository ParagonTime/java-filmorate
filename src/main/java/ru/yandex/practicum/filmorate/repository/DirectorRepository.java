package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.Collection;
import java.util.List;

@Repository
public class DirectorRepository extends BaseRepository<DirectorDto>{

    private static final String INSERT_QUERY = "INSERT INTO directors(name) VALUES (?)";
    private static final String FIND_BY_ID = "SELECT * FROM directors WHERE id = ?";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String FIND_ALL_DIRECTORS = "SELECT * FROM directors";
    private static final String DELETE_DIRECTOR = "DELETE FROM directors WHERE id = ?";
    private static final String INSERT_FILM_DIRECTOR = "INSERT INTO film_director(film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS = "DELETE FROM film_director WHERE film_id = ?";
    private static final String FIND_DIRECTORS_BY_FILM = "SELECT d.* FROM film_director fd JOIN directors d ON fd.director_id = d.id WHERE fd.film_id = ?";

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<DirectorDto> mapper) {
        super(jdbc, mapper);
    }

    public DirectorDto save(DirectorDto director) {
        Long id = insert(
                INSERT_QUERY,
                director.getName()
        );
        director.setId(id);
        return director;
    }

    public DirectorDto getDirector(Long id) {
        return findOne(FIND_BY_ID, id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с таким id "+ id +" не найден"));
    }

    public DirectorDto update(DirectorDto director) {
        update(
                UPDATE_QUERY,
                director.getName(),
                director.getId()
        );
        return director;
    }

    public Collection<DirectorDto> getDirectors() {
        return findMany(FIND_ALL_DIRECTORS);
    }

    public Boolean deleteDirector(Long id) {
        return update(DELETE_DIRECTOR, id) > 0;
    }

    public List<DirectorDto> getDirectorsByFilm(Long filmId) {
        return findMany(FIND_DIRECTORS_BY_FILM, filmId);
    }

    public void saveDirector(Long filmId, Long directorId) {
        update(INSERT_FILM_DIRECTOR, filmId, directorId);
    }

    public void deleteDirectors(Long filmId) {
        update(DELETE_FILM_DIRECTORS, filmId);
    }
}
