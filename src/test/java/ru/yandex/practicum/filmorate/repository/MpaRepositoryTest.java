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
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.mapper.MpaRowMapper;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaRepository.class, MpaRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MpaRepositoryTest {
    private final MpaRepository mpaRepository;

    @Test
    @Order(1)
    public void testGetAllMpa() {
        Collection<MpaDto> mpaList = mpaRepository.getAllMpa();
        assertEquals(5, mpaList.size());
    }

    @Test
    @Order(2)
    public void testGetMpaByExistId() {
        Optional<MpaDto> mpa = mpaRepository.getMpaById(1);
        mpa.ifPresent(mpaDto -> assertEquals("G", mpaDto.getName()));
    }

    @Test
    @Order(3)
    public void testGetMpaByNotExistId() {
        assertFalse(mpaRepository.getMpaById(100).isPresent());
    }

    @Test
    @Order(4)
    public void testGetMpaByNotExistFilm() {
        assertFalse(mpaRepository.getMpaByFilmId(100).isPresent());
    }
}