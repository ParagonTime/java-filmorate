package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreRepository.class, GenreRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GenreRepositoryTest {
    private final GenreRepository genreRepository;

    @Test
    @Order(1)
    public void testGetGenreById() {
        Optional<GenreDto> genre = genreRepository.getGenreById(1L);
        assertTrue(genre.isPresent());
        assertEquals(1L, genre.get().getId());
        assertNotNull(genre.get().getName());
    }

    @Test
    @Order(2)
    public void testGetGenreByInvalidId() {
        Optional<GenreDto> genre = genreRepository.getGenreById(999L);
        assertFalse(genre.isPresent());
    }

    @Test
    @Order(3)
    public void testGetAllGenres() {
        List<GenreDto> genres = genreRepository.getAllGenres();
        assertNotNull(genres);
        assertFalse(genres.isEmpty());
        assertTrue(genres.size() >= 6);
    }

    @Test
    @Order(4)
    public void testGetAllGenresByFilmId() {
        List<GenreDto> genres = genreRepository.getAllGenresByFilmId(1L);
        assertNotNull(genres);
    }
}