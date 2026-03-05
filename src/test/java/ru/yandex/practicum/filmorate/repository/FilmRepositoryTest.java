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
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmRepository.class, FilmRowMapper.class,
        MpaRowMapper.class, GenreRepository.class,
        GenreRowMapper.class, UserRepository.class,
        UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FilmRepositoryTest {
    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private static Film film;
    private static User user;
    private static int nameCount;
    private static int mailCount;

    public static String getNewName() {
        ++nameCount;
        return "Film" + nameCount;
    }

    public static String getNewMail() {
        ++mailCount;
        return "mail" + mailCount + "@mail.mail";
    }

    @BeforeAll
    public static void start() {
        nameCount = 0;
        mailCount = 0;

        film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setRatingId(1L);
        film.setGenresIds(new HashSet<>());

        user = new User();
        user.setName("User");
        user.setLogin("Login");
        user.setEmail("Email");
        user.setBirthday(LocalDate.now());
    }

    @Test
    @Order(1)
    public void testFindFilmById() {
        assertThrows(NotFoundException.class, () -> filmRepository.getFilm(100L));
    }

    @Test
    @Order(2)
    public void testAddFilm() {
        assertThrows(NotFoundException.class, () -> filmRepository.getFilm(1L));
        assertEquals(1L, filmRepository.save(film).getId());
    }

    @Test
    @Order(3)
    public void testAddFilmAndPutNewFields() {
        film.setName(getNewName());
        Long idFilm = filmRepository.save(film).getId();
        assertEquals(2L, idFilm);

        String newName = "Updated Film";
        String newDescription = "Updated Description";
        LocalDate newDate = LocalDate.of(2010, 1, 1);
        Integer newDuration = 150;
        Long newRatingId = 2L;

        film.setId(idFilm);
        film.setName(newName);
        film.setDescription(newDescription);
        film.setReleaseDate(newDate);
        film.setDuration(newDuration);
        film.setRatingId(newRatingId);

        Film updatedFilm = filmRepository.update(film);
        assertEquals(newName, updatedFilm.getName());
        assertEquals(newDescription, updatedFilm.getDescription());
        assertEquals(newDate, updatedFilm.getReleaseDate());
        assertEquals(newDuration, updatedFilm.getDuration());
        assertEquals(newRatingId, updatedFilm.getRatingId());
    }

    @Test
    @Order(4)
    public void testAddFilmWithGenres() {
        film.setName(getNewName());
        Film savedFilm = filmRepository.save(film);

        Set<Long> genreIds = new HashSet<>();
        genreIds.add(1L);
        genreIds.add(2L);

        for (Long genreId : genreIds) {
            filmRepository.saveGenres(savedFilm.getId(), genreId);
        }

        List<GenreDto> genres = genreRepository.getAllGenresByFilmId(savedFilm.getId());
        assertEquals(2, genres.size());
    }

    @Test
    @Order(5)
    public void testUpdateFilmGenres() {
        film.setName(getNewName());
        Film savedFilm = filmRepository.save(film);

        Set<Long> genreIds = new HashSet<>();
        genreIds.add(1L);
        genreIds.add(2L);

        for (Long genreId : genreIds) {
            filmRepository.saveGenres(savedFilm.getId(), genreId);
        }

        List<GenreDto> genres = genreRepository.getAllGenresByFilmId(savedFilm.getId());
        assertEquals(2, genres.size());

        filmRepository.deleteGenres(savedFilm.getId());

        Set<Long> newGenreIds = new HashSet<>();
        newGenreIds.add(3L);
        newGenreIds.add(4L);
        newGenreIds.add(5L);

        for (Long genreId : newGenreIds) {
            filmRepository.saveGenres(savedFilm.getId(), genreId);
        }

        List<GenreDto> updatedGenres = genreRepository.getAllGenresByFilmId(savedFilm.getId());
        assertEquals(3, updatedGenres.size());
    }

    @Test
    @Order(6)
    public void testAddLike() {
        film.setName(getNewName());
        Film savedFilm = filmRepository.save(film);

        userRepository.save(user);

        user.setEmail(getNewMail());
        userRepository.save(user);

        Boolean result = filmRepository.addLike(savedFilm.getId(), 1L);
        assertTrue(result);
    }

    @Test
    @Order(7)
    public void testDeleteLike() {
        film.setName(getNewName());
        Film savedFilm = filmRepository.save(film);
        user.setEmail(getNewMail());
        Long userId = userRepository.save(user).getId();

        filmRepository.addLike(savedFilm.getId(), userId);
        Boolean result = filmRepository.deleteLike(savedFilm.getId(), userId);
        assertTrue(result);
    }

    @Test
    @Order(8)
    public void testGetPopularFilms() {
        film.setName(getNewName());
        Film film1 = filmRepository.save(film);

        film.setName(getNewName());
        Film film2 = filmRepository.save(film);

        film.setName(getNewName());
        Film film3 = filmRepository.save(film);

        user.setEmail(getNewMail());
        Long userOneId = userRepository.save(user).getId();
        filmRepository.addLike(film2.getId(), userOneId);

        user.setEmail(getNewMail());
        Long userTwoId = userRepository.save(user).getId();
        filmRepository.addLike(film2.getId(), userTwoId);

        user.setEmail(getNewMail());
        Long userThreeId = userRepository.save(user).getId();
        filmRepository.addLike(film3.getId(), userThreeId);

        Collection<Film> popularFilms = filmRepository.getPopularFilms(2L);
        assertEquals(2, popularFilms.size());

        Film firstFilm = popularFilms.iterator().next();
        assertTrue(firstFilm.getId().equals(film2.getId()) || firstFilm.getId().equals(film3.getId()));
    }

    @Test
    @Order(9)
    public void testGetAllFilms() {
        film.setName(getNewName());
        filmRepository.save(film);
        film.setName(getNewName());
        filmRepository.save(film);

        Collection<Film> films = filmRepository.getFilms();
        assertTrue(films.size() >= 2);
    }
}