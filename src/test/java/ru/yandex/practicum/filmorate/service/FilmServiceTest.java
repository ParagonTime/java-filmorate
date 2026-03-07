package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FilmServiceTest {
    private final FilmService filmService;
    private final UserService userService;
    private static NewFilmRequest newFilm;
    private static NewFilmRequest filmWithGenres;
    private static NewUserRequest user;
    private static NewUserRequest userTwo;
    private static int emailCount;
    private static int filmCount;

    public static String getNewEmail() {
        ++emailCount;
        return "email" + emailCount + "@test.com";
    }

    public static String getNewFilmName() {
        ++filmCount;
        return "Film " + filmCount;
    }

    @BeforeAll
    public static void start() {
        emailCount = 0;
        filmCount = 0;

        user = new NewUserRequest();
        user.setName("User");
        user.setLogin("Login");
        user.setEmail(getNewEmail());
        user.setBirthday(LocalDate.of(1990, 1, 1));

        userTwo = new NewUserRequest();
        userTwo.setName("Another User");
        userTwo.setLogin("AnotherLogin");
        userTwo.setEmail(getNewEmail());
        userTwo.setBirthday(LocalDate.of(1990, 1, 1));

        newFilm = new NewFilmRequest();
        newFilm.setName(getNewFilmName());
        newFilm.setDescription("Description");
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilm.setDuration(120);
        MpaDto mpa = new MpaDto();
        mpa.setId(1L);
        newFilm.setMpa(mpa);

        filmWithGenres = new NewFilmRequest();
        filmWithGenres.setName(getNewFilmName());
        filmWithGenres.setDescription("Description with genres");
        filmWithGenres.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmWithGenres.setDuration(120);
        MpaDto mpaWithGenres = new MpaDto();
        mpaWithGenres.setId(1L);
        filmWithGenres.setMpa(mpaWithGenres);

        GenreDto comedy = new GenreDto();
        comedy.setId(1L);
        GenreDto drama = new GenreDto();
        drama.setId(2L);
        filmWithGenres.setGenres(List.of(comedy, drama));
    }

    @Test
    @Order(1)
    public void testGetFilmByNotExistId() {
        assertThrows(NotFoundException.class, () -> filmService.getFilm(999L));
    }

    @Test
    @Order(2)
    public void testPostFilm() {
        FilmDto createdFilm = filmService.postFilm(newFilm);
        assertNotNull(createdFilm.getId());
        assertEquals(newFilm.getName(), createdFilm.getName());
        assertEquals(newFilm.getDescription(), createdFilm.getDescription());
        assertEquals(newFilm.getReleaseDate(), createdFilm.getReleaseDate());
        assertEquals(newFilm.getDuration(), createdFilm.getDuration());
        assertNotNull(createdFilm.getMpa());
        assertEquals(newFilm.getMpa().getId(), createdFilm.getMpa().getId());
    }

    @Test
    @Order(3)
    public void testPostFilmWithInvalidReleaseDate() {
        NewFilmRequest invalidRequest = new NewFilmRequest();
        invalidRequest.setName(getNewFilmName());
        invalidRequest.setDescription("Description");
        invalidRequest.setReleaseDate(LocalDate.of(1800, 1, 1));
        invalidRequest.setDuration(120);
        MpaDto mpa = new MpaDto();
        mpa.setId(1L);
        invalidRequest.setMpa(mpa);

        assertThrows(ValidationException.class, () -> filmService.postFilm(invalidRequest));
    }

    @Test
    @Order(4)
    public void testPostFilmWithInvalidGenre() {
        NewFilmRequest invalidRequest = new NewFilmRequest();
        invalidRequest.setName(getNewFilmName());
        invalidRequest.setDescription("Description");
        invalidRequest.setReleaseDate(LocalDate.of(2000, 1, 1));
        invalidRequest.setDuration(120);
        MpaDto mpa = new MpaDto();
        mpa.setId(1L);
        invalidRequest.setMpa(mpa);

        GenreDto invalidGenre = new GenreDto();
        invalidGenre.setId(999L);
        invalidRequest.setGenres(List.of(invalidGenre));

        assertThrows(NotFoundException.class, () -> filmService.postFilm(invalidRequest));
    }

    @Test
    @Order(5)
    public void testPostFilmWithInvalidMpa() {
        NewFilmRequest invalidRequest = new NewFilmRequest();
        invalidRequest.setName(getNewFilmName());
        invalidRequest.setDescription("Description");
        invalidRequest.setReleaseDate(LocalDate.of(2000, 1, 1));
        invalidRequest.setDuration(120);
        MpaDto mpa = new MpaDto();
        mpa.setId(999L);
        invalidRequest.setMpa(mpa);

        assertThrows(NotFoundException.class, () -> filmService.postFilm(invalidRequest));
    }

    @Test
    @Order(6)
    public void testPostFilmWithGenres() {
        FilmDto createdFilm = filmService.postFilm(filmWithGenres);
        assertNotNull(createdFilm.getId());
        assertEquals(2, createdFilm.getGenres().size());
    }

    @Test
    @Order(7)
    public void testPutFilm() {
        FilmDto createdFilm = filmService.postFilm(newFilm);

        UpdateFilmRequest updateRequest = new UpdateFilmRequest();
        updateRequest.setId(createdFilm.getId());
        updateRequest.setName("Updated Film Name");
        updateRequest.setDescription("Updated Description");
        updateRequest.setReleaseDate(LocalDate.of(2010, 1, 1));
        updateRequest.setDuration(150);
        MpaDto mpa = new MpaDto();
        mpa.setId(2L);
        updateRequest.setMpa(mpa);

        FilmDto updatedFilm = filmService.putFilm(updateRequest);
        assertEquals(updateRequest.getId(), updatedFilm.getId());
        assertEquals(updateRequest.getName(), updatedFilm.getName());
        assertEquals(updateRequest.getDescription(), updatedFilm.getDescription());
        assertEquals(updateRequest.getReleaseDate(), updatedFilm.getReleaseDate());
        assertEquals(updateRequest.getDuration(), updatedFilm.getDuration());
        assertEquals(updateRequest.getMpa().getId(), updatedFilm.getMpa().getId());
    }

    @Test
    @Order(8)
    public void testPutFilmWithGenres() {
        FilmDto createdFilm = filmService.postFilm(newFilm);

        UpdateFilmRequest updateRequest = new UpdateFilmRequest();
        updateRequest.setId(createdFilm.getId());
        updateRequest.setName(createdFilm.getName());

        GenreDto comedy = new GenreDto();
        comedy.setId(1L);
        GenreDto drama = new GenreDto();
        drama.setId(2L);
        GenreDto action = new GenreDto();
        action.setId(6L);
        updateRequest.setGenres(List.of(comedy, drama, action));

        FilmDto updatedFilm = filmService.putFilm(updateRequest);
        assertEquals(3, updatedFilm.getGenres().size());
    }

    @Test
    @Order(9)
    public void testGetFilm() {
        FilmDto createdFilm = filmService.postFilm(newFilm);
        FilmDto foundFilm = filmService.getFilm(createdFilm.getId());
        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals(createdFilm.getName(), foundFilm.getName());
    }

    @Test
    @Order(10)
    public void testGetFilms() {
        filmService.postFilm(newFilm);
        filmService.postFilm(filmWithGenres);

        Collection<FilmDto> films = filmService.getFilms();
        assertTrue(films.size() >= 2);
    }

    @Test
    @Order(11)
    public void testAddLike() {
        FilmDto film = filmService.postFilm(newFilm);
        UserDto userDto = userService.postUser(user);

        Boolean result = filmService.addLike(film.getId(), userDto.getId());
        assertTrue(result);

        FilmDto filmWithLike = filmService.getFilm(film.getId());
    }

    @Test
    @Order(12)
    public void testDeleteLike() {
        FilmDto film = filmService.postFilm(newFilm);
        user.setEmail(getNewEmail());
        UserDto userDto = userService.postUser(user);

        filmService.addLike(film.getId(), userDto.getId());
        Boolean deleteResult = filmService.deleteLike(film.getId(), userDto.getId());
        assertTrue(deleteResult);
    }

    @Test
    @Order(13)
    public void testGetPopularFilms() {
        FilmDto film1 = filmService.postFilm(newFilm);

        NewFilmRequest anotherFilm = new NewFilmRequest();
        anotherFilm.setName(getNewFilmName());
        anotherFilm.setDescription("Another film");
        anotherFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        anotherFilm.setDuration(120);
        MpaDto mpa = new MpaDto();
        mpa.setId(1L);
        anotherFilm.setMpa(mpa);
        FilmDto film2 = filmService.postFilm(anotherFilm);

        NewFilmRequest thirdFilm = new NewFilmRequest();
        thirdFilm.setName(getNewFilmName());
        thirdFilm.setDescription("Third film");
        thirdFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        thirdFilm.setDuration(120);
        thirdFilm.setMpa(mpa);
        FilmDto film3 = filmService.postFilm(thirdFilm);

        user.setEmail(getNewEmail());
        UserDto userDtoOne = userService.postUser(user);
        userTwo.setEmail(getNewEmail());
        UserDto userDtoTwo = userService.postUser(userTwo);

        filmService.addLike(film2.getId(), userDtoOne.getId());
        filmService.addLike(film2.getId(), userDtoTwo.getId());
        filmService.addLike(film3.getId(), userDtoOne.getId());

        Collection<FilmDto> popularFilms = filmService.getPopularFilms(2L);
        assertEquals(2, popularFilms.size());
    }
}