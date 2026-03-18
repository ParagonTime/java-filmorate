package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@Import({DirectorRepository.class, FilmRepository.class,
        DirectorDto.class, Film.class,
        FilmRowMapper.class, DirectorRowMapper.class,})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DirectorRepositoryTest {
    private final DirectorRepository directorRepository;
    private final FilmRepository filmRepository;
    private static Film film;
    private static DirectorDto director;

    @BeforeAll
    public static void start() {
        film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setDuration(120);
        film.setReleaseDate(LocalDate.of(1990, 3, 1));
        film.setRatingId(1L);

        director = new DirectorDto();
        director.setName("First Director");
    }

    @Test
    @Order(1)
    public void testSaveDirector() {
        DirectorDto savedDirector = directorRepository.save(director);
        assertEquals(1L, savedDirector.getId());
        assertEquals("First Director", savedDirector.getName());
    }

    @Test
    @Order(2)
    public void testUpdateDirector() {
        director.setName("Updated Director");
        DirectorDto updatedDirector = directorRepository.update(director);
        assertEquals(1L, updatedDirector.getId());
        assertEquals("Updated Director", updatedDirector.getName());
    }

    @Test
    @Order(3)
    public void testGetDirectors() {
        directorRepository.save(director);
        assertEquals(1, directorRepository.getDirectors().size());
    }

    @Test
    @Order(4)
    public void testGetDirectorByDirectorId() {
        DirectorDto director1 = directorRepository.save(director);
        DirectorDto directorDto = directorRepository.getDirector(director1.getId());
        assertEquals(director.getId(), directorDto.getId());
        assertEquals(director.getName(), directorDto.getName());
    }

    @Test
    @Order(5)
    public void testAddDirectorForFilm() {
        DirectorDto directorDto = directorRepository.save(director);
        Long filmId = filmRepository.save(film).getId();
        film.setId(filmId);
        directorRepository.saveDirectorForFilm(filmId, directorDto.getId());
        List<DirectorDto> filmDirectors = directorRepository.getDirectorsByFilm(filmId);
        assertEquals(director.getId(), filmDirectors.getFirst().getId());
        assertEquals(director.getName(), filmDirectors.getFirst().getName());
    }

    @Test
    @Order(6)
    public void testDeleteDirector() {
        DirectorDto secondDirector = new DirectorDto();
        secondDirector.setName("Second Director");
        Long secondDirId = directorRepository.save(secondDirector).getId();
        secondDirector.setId(secondDirId);

        assertTrue(directorRepository.deleteDirector(secondDirId));
        assertFalse(directorRepository.deleteDirector(secondDirId));
    }

    @Test
    @Order(7)
    public void testDeleteDirectors() {
        directorRepository.deleteDirectors(film.getId());
        List<DirectorDto> directors = directorRepository.getDirectors();
        assertEquals(0, directors.size());
    }

}