package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DirectorServiceTest {
    private final DirectorService directorService;

    @Test
    @Order(1)
    public void testCreateDirector() {
        NewDirectorRequest directorRequest = new NewDirectorRequest();
        directorRequest.setName("First");

        DirectorDto createdDirector = directorService.createDirector(directorRequest);
        assertEquals("First", createdDirector.getName());
    }

    @Test
    @Order(2)
    public void testUpdateDirector() {
        NewDirectorRequest directorRequest = new NewDirectorRequest();
        directorRequest.setName("Second");

        DirectorDto createdDirector = directorService.createDirector(directorRequest);
        assertEquals("Second", createdDirector.getName());

        UpdateDirectorRequest updateDirector = new UpdateDirectorRequest();
        updateDirector.setId(createdDirector.getId());
        updateDirector.setName("Update");
        createdDirector = directorService.updateDirector(updateDirector);
        assertEquals("Update", createdDirector.getName());
    }

    @Test
    @Order(3)
    public void testGetDirectors() {
        Collection<DirectorDto> directors = directorService.getDirectors();
        assertFalse(directors.isEmpty());
    }

    @Test
    @Order(4)
    public void testGetDirector() {
        DirectorDto firstDirector = directorService.getDirector(1L);
        assertEquals("First", firstDirector.getName());

        DirectorDto secondDirector = directorService.getDirector(2L);
        assertEquals("Second", secondDirector.getName());
    }

    @Test
    @Order(5)
    public void testDeleteDirector() {
        int startSizeDirectors = directorService.getDirectors().size();
        assertTrue(directorService.deleteDirector(1L));
        assertTrue(directorService.deleteDirector(2L));
        assertFalse(directorService.deleteDirector(1L));
        assertEquals(directorService.getDirectors().size(), startSizeDirectors - 2);
    }
}