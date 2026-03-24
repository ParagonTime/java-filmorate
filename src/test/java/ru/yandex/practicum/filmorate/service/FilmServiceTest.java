package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
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
    private final DirectorService directorService;
    private static NewFilmRequest newFilm;
    private static NewFilmRequest filmWithAllFields;
    private static NewUserRequest user;
    private static NewUserRequest userTwo;
    private static DirectorDto directorDtoOne;
    private static DirectorDto directorDtoTwo;
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

        filmWithAllFields = new NewFilmRequest();
        filmWithAllFields.setName(getNewFilmName());
        filmWithAllFields.setDescription("Description with genres");
        filmWithAllFields.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmWithAllFields.setDuration(120);
        MpaDto mpaWithGenres = new MpaDto();
        mpaWithGenres.setId(1L);
        filmWithAllFields.setMpa(mpaWithGenres);

        GenreDto comedy = new GenreDto();
        comedy.setId(1L);
        GenreDto drama = new GenreDto();
        drama.setId(2L);
        filmWithAllFields.setGenres(List.of(comedy, drama));

        directorDtoOne = new DirectorDto();
        directorDtoOne.setId(1L);
        directorDtoOne.setName("First Director");
        directorDtoTwo = new DirectorDto();
        directorDtoTwo.setId(2L);
        directorDtoTwo.setName("Second Director");
        filmWithAllFields.setDirectors(List.of(directorDtoOne, directorDtoTwo));
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
        NewDirectorRequest dReq1 = new NewDirectorRequest();
        dReq1.setName("First");
        DirectorDto createdDirector1 = directorService.createDirector(dReq1);

        NewDirectorRequest dReq2 = new NewDirectorRequest();
        dReq2.setName("Second");
        DirectorDto createdDirector2 = directorService.createDirector(dReq2);

        DirectorDto directorDto1 = new DirectorDto();
        directorDto1.setId(createdDirector1.getId());
        directorDto1.setName(createdDirector1.getName());

        DirectorDto directorDto2 = new DirectorDto();
        directorDto2.setId(createdDirector2.getId());
        directorDto2.setName(createdDirector2.getName());

        filmWithAllFields.setDirectors(List.of(directorDto1, directorDto2));

        FilmDto createdFilm = filmService.postFilm(filmWithAllFields);
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
        NewDirectorRequest directorRequest = new NewDirectorRequest();
        directorRequest.setName("First Director");
        directorService.createDirector(directorRequest);
        directorRequest.setName("Second Director");
        directorService.createDirector(directorRequest);
        filmService.postFilm(newFilm);
        filmService.postFilm(filmWithAllFields);

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
    @Order(14)
    public void testGetFilmsWithGenreByYear() {
        GenreDto genre = new GenreDto();
        genre.setId(4L);
        newFilm.setGenres(List.of(genre));
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmWithAllFields.setGenres(List.of(genre));
        filmWithAllFields.setReleaseDate(LocalDate.of(2000, 1, 1));

        FilmDto filmDto1 = filmService.postFilm(newFilm);
        FilmDto filmDto2 = filmService.postFilm(filmWithAllFields);

        user.setEmail(getNewEmail());
        userTwo.setEmail(getNewEmail());

        UserDto userDto1 = userService.postUser(user);
        UserDto userDto2 = userService.postUser(userTwo);

        filmService.addLike(filmDto1.getId(), userDto1.getId());
        filmService.addLike(filmDto1.getId(), userDto2.getId());
        filmService.addLike(filmDto2.getId(), userDto1.getId());

        Collection<FilmDto> films = filmService.getPopularWithGenreByYear(10, 4L, 2000);

        assertEquals(2, films.size());
        assertEquals(filmDto1.getId(), films.stream().toList().getFirst().getId());
    }

    @Test
    @Order(16)
    public void testGetFilmsByDirector() {
        NewDirectorRequest directorRequest = new NewDirectorRequest();
        directorRequest.setName("Director 1");
        DirectorDto createdDirector = directorService.createDirector(directorRequest);
        System.out.println("Created director with id: " + createdDirector.getId());

        DirectorDto directorForFilm = new DirectorDto();
        directorForFilm.setId(createdDirector.getId());
        directorForFilm.setName(createdDirector.getName());
        newFilm.setDirectors(List.of(directorForFilm));

        FilmDto filmOne = filmService.postFilm(newFilm);
        System.out.println("Created film 1 with id: " + filmOne.getId() + ", directors: " + filmOne.getDirectors());

        FilmDto filmTwo = filmService.postFilm(newFilm);
        System.out.println("Created film 2 with id: " + filmTwo.getId() + ", directors: " + filmTwo.getDirectors());

        Collection<FilmDto> filmDirectorSortYear = filmService.getFilmsByDirector(createdDirector.getId(), "year");
        System.out.println("Films by year: " + filmDirectorSortYear.size());
        assertEquals(2, filmDirectorSortYear.size());

        Collection<FilmDto> filmDirectorSortLikes = filmService.getFilmsByDirector(createdDirector.getId(), "likes");
        System.out.println("Films by likes: " + filmDirectorSortLikes.size());
        assertEquals(2, filmDirectorSortLikes.size());
    }

    @Test
    @Order(15)
    public void testDeleteFilm() {
        FilmDto createdFilm = filmService.postFilm(newFilm);
        Long filmId = createdFilm.getId();

        FilmDto foundFilm = filmService.getFilm(filmId);
        assertNotNull(foundFilm);

        filmService.deleteFilm(filmId);

        assertThrows(NotFoundException.class, () -> filmService.getFilm(filmId));
    }

    @Test
    @Order(16)
    public void testDeleteFilmWithLikes() {
        FilmDto createdFilm = filmService.postFilm(newFilm);
        Long filmId = createdFilm.getId();

        user.setEmail(getNewEmail());
        UserDto createdUser = userService.postUser(user);

        filmService.addLike(filmId, createdUser.getId());

        Collection<FilmDto> popularFilms = filmService.getPopularWithGenreByYear(10, null, null);
        boolean hasLike = popularFilms.stream().anyMatch(f -> f.getId().equals(filmId));
        assertTrue(hasLike);

        filmService.deleteFilm(filmId);

        assertThrows(NotFoundException.class, () -> filmService.getFilm(filmId));

        UserDto existingUser = userService.getUser(createdUser.getId());
        assertNotNull(existingUser);
    }

    @Test
    @Order(17)
    public void testDeleteFilmWithGenres() {
        FilmDto createdFilm = filmService.postFilm(filmWithAllFields);
        Long filmId = createdFilm.getId();

        assertTrue(createdFilm.getGenres().size() > 0);

        filmService.deleteFilm(filmId);

        assertThrows(NotFoundException.class, () -> filmService.getFilm(filmId));
    }

    @Test
    @Order(18)
    public void testDeleteFilmWithDirectors() {
        NewDirectorRequest directorRequest = new NewDirectorRequest();
        directorRequest.setName("Test Director for Delete");
        DirectorDto createdDirector = directorService.createDirector(directorRequest);

        NewFilmRequest filmWithDirector = new NewFilmRequest();
        filmWithDirector.setName(getNewFilmName());
        filmWithDirector.setDescription("Film with director");
        filmWithDirector.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmWithDirector.setDuration(120);
        MpaDto mpa = new MpaDto();
        mpa.setId(1L);
        filmWithDirector.setMpa(mpa);

        DirectorDto directorForFilm = new DirectorDto();
        directorForFilm.setId(createdDirector.getId());
        filmWithDirector.setDirectors(List.of(directorForFilm));

        FilmDto createdFilm = filmService.postFilm(filmWithDirector);
        Long filmId = createdFilm.getId();

        assertEquals(1, createdFilm.getDirectors().size());

        filmService.deleteFilm(filmId);

        assertThrows(NotFoundException.class, () -> filmService.getFilm(filmId));

        DirectorDto existingDirector = directorService.getDirector(createdDirector.getId());
        assertNotNull(existingDirector);
    }

    @Test
    @Order(19)
    public void testDeleteNonExistentFilm() {
        assertThrows(NotFoundException.class, () -> filmService.deleteFilm(999L));
    }

    @Test
    @Order(20)
    public void testDeleteFilmAndVerifyCascadeDelete() {
        FilmDto createdFilm = filmService.postFilm(newFilm);
        Long filmId = createdFilm.getId();

        user.setEmail(getNewEmail());
        UserDto createdUser = userService.postUser(user);
        filmService.addLike(filmId, createdUser.getId());

        filmService.deleteFilm(filmId);

        assertThrows(NotFoundException.class, () -> filmService.getFilm(filmId));

        UserDto existingUser = userService.getUser(createdUser.getId());
        assertNotNull(existingUser);
    }
}