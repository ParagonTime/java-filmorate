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
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorRowMapper;
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

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmRepository.class, FilmRowMapper.class,
        MpaRowMapper.class, GenreRepository.class,
        GenreRowMapper.class, UserRepository.class,
        UserRowMapper.class, DirectorRepository.class,
        DirectorDto.class, DirectorRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FilmRepositoryTest {
    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final DirectorRepository directorRepository;
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
        film.setDirectorsIds(new HashSet<>(List.of(1L, 2L)));

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

    @Test
    @Order(11)
    public void testGetFilmsLikedByUser() {
        User testUser = new User();
        testUser.setName("Test User");
        testUser.setLogin("testLogin" + System.currentTimeMillis());
        testUser.setEmail("test" + System.currentTimeMillis() + "@test.com");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser = userRepository.save(testUser);

        Film testFilm1 = new Film();
        testFilm1.setName("Test Film 1 " + System.currentTimeMillis());
        testFilm1.setDescription("Description 1");
        testFilm1.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm1.setDuration(120);
        testFilm1.setRatingId(1L);
        Film film1 = filmRepository.save(testFilm1);

        Film testFilm2 = new Film();
        testFilm2.setName("Test Film 2 " + System.currentTimeMillis());
        testFilm2.setDescription("Description 2");
        testFilm2.setReleaseDate(LocalDate.of(2001, 1, 1));
        testFilm2.setDuration(130);
        testFilm2.setRatingId(1L);
        Film film2 = filmRepository.save(testFilm2);

        Film testFilm3 = new Film();
        testFilm3.setName("Test Film 3 " + System.currentTimeMillis());
        testFilm3.setDescription("Description 3");
        testFilm3.setReleaseDate(LocalDate.of(2002, 1, 1));
        testFilm3.setDuration(140);
        testFilm3.setRatingId(2L);
        Film film3 = filmRepository.save(testFilm3);

        Boolean like1 = filmRepository.addLike(film1.getId(), savedUser.getId());
        Boolean like2 = filmRepository.addLike(film2.getId(), savedUser.getId());

        assertTrue(like1);
        assertTrue(like2);

        Collection<Film> likedFilms = filmRepository.getFilmsLikedByUser(savedUser.getId());

        assertEquals(2, likedFilms.size());
        assertTrue(likedFilms.stream().anyMatch(f -> f.getId().equals(film1.getId())));
        assertTrue(likedFilms.stream().anyMatch(f -> f.getId().equals(film2.getId())));
        assertTrue(likedFilms.stream().noneMatch(f -> f.getId().equals(film3.getId())));
    }

    @Test
    @Order(12)
    public void testDeleteFilm() {
        film.setName(getNewName());
        Film savedFilm = filmRepository.save(film);
        Long filmId = savedFilm.getId();

        Film foundFilm = filmRepository.getFilm(filmId);
        assertNotNull(foundFilm);

        boolean deleted = filmRepository.deleteFilm(filmId);
        assertTrue(deleted);

        assertThrows(NotFoundException.class, () -> filmRepository.getFilm(filmId));
    }

    @Test
    @Order(13)
    public void testDeleteFilmWithGenres() {
        film.setName(getNewName());
        Film savedFilm = filmRepository.save(film);
        Long filmId = savedFilm.getId();

        filmRepository.saveGenres(filmId, 1L);
        filmRepository.saveGenres(filmId, 2L);

        List<GenreDto> genresBefore = genreRepository.getAllGenresByFilmId(filmId);
        assertEquals(2, genresBefore.size());

        boolean deleted = filmRepository.deleteFilm(filmId);
        assertTrue(deleted);

        List<GenreDto> genresAfter = genreRepository.getAllGenresByFilmId(filmId);
        assertEquals(0, genresAfter.size());
    }

    @Test
    @Order(14)
    public void testDeleteFilmWithLikes() {
        film.setName(getNewName());
        Film savedFilm = filmRepository.save(film);
        Long filmId = savedFilm.getId();

        user.setEmail(getNewMail());
        User savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        filmRepository.addLike(filmId, userId);

        boolean deleted = filmRepository.deleteFilm(filmId);
        assertTrue(deleted);

        assertThrows(NotFoundException.class, () -> filmRepository.getFilm(filmId));

        User existingUser = userRepository.getUser(userId);
        assertNotNull(existingUser);
    }

    @Test
    @Order(15)
    public void testDeleteFilmWithDirectors() {
        DirectorDto director = new DirectorDto();
        director.setName("Test Director");
        DirectorDto savedDirector = directorRepository.save(director);

        film.setName(getNewName());
        Film savedFilm = filmRepository.save(film);
        Long filmId = savedFilm.getId();

        directorRepository.saveDirectorForFilm(filmId, savedDirector.getId());

        List<DirectorDto> directorsBefore = directorRepository.getDirectorsByFilm(filmId);
        assertEquals(1, directorsBefore.size());

        boolean deleted = filmRepository.deleteFilm(filmId);
        assertTrue(deleted);

        List<DirectorDto> directorsAfter = directorRepository.getDirectorsByFilm(filmId);
        assertEquals(0, directorsAfter.size());

        DirectorDto existingDirector = directorRepository.getDirector(savedDirector.getId());
        assertNotNull(existingDirector);
    }

    @Test
    @Order(16)
    public void testDeleteNonExistentFilm() {
        boolean deleted = filmRepository.deleteFilm(999L);
        assertFalse(deleted);
    }

    @Test
    @Order(10)
    public void testGetFilmsByDirector() {
        DirectorDto director = new DirectorDto();
        director.setName("First Director");
        DirectorDto savedDirector = directorRepository.save(director);
        Long filmId = filmRepository.save(film).getId();
        directorRepository.saveDirectorForFilm(filmId, 1L);
        Collection<Film> filmsDirectorSortYear = filmRepository.getFilmsByDirector(1L, "year");
        assertEquals(1, filmsDirectorSortYear.size());
        Collection<Film> filmsDirectorSortLikes = filmRepository.getFilmsByDirector(1L, "likes");
        assertEquals(1, filmsDirectorSortLikes.size());
    }

    @Test
    @Order(17)
    public void testGetFilmsWithGenreByYear() {
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        user.setEmail(getNewMail());
        User user1 = userRepository.save(user);
        System.out.println(user1);

        Film film1 = filmRepository.save(film);
        System.out.println(film1);
        filmRepository.saveGenres(film1.getId(), 4L);
        filmRepository.addLike(film1.getId(), user1.getId());

        Film film2 = filmRepository.save(film);
        System.out.println(film2);
        filmRepository.saveGenres(film2.getId(), 4L);

        filmRepository.addLike(film2.getId(), user1.getId());

        user.setEmail(getNewMail());
        User user2 = userRepository.save(user);
        System.out.println(user2);
        filmRepository.addLike(film1.getId(), user2.getId());

        Collection<Film> films = filmRepository.getFilmsWithGenreByYear(10, 4L, 2000);
        assertEquals(2, films.size());
        assertEquals(film1.getId(), films.stream().toList().getFirst().getId());
    }

    @Test
    @Order(18)
    public void testSearchFilmsByDirector() {
        DirectorDto director = new DirectorDto();
        director.setName("Sixteenth Test Director"); // когда-то это был 16-й тест, потом сместилось. Текст править не стал
        DirectorDto savedDirector = directorRepository.save(director);
        Long filmId = filmRepository.save(film).getId();
        directorRepository.saveDirectorForFilm(filmId, savedDirector.getId());
        Collection<Film> filmsSearchedByDirector = filmRepository.getSearchFilms("sixteen", true, false);
        assertEquals(1, filmsSearchedByDirector.size());
    }

    @Test
    @Order(19)
    public void testSearchFilmsByTitle() {
        film.setName("Seventeenth Test Film"); // когда-то это был 17-й тест, потом сместилось. Текст править не стал
        Long filmId = filmRepository.save(film).getId();
        Collection<Film> filmsSearchedByTitle = filmRepository.getSearchFilms("seven", false, true);
        assertNotEquals(0, filmsSearchedByTitle.size());
    }

    @Test
    @Order(20)
    public void testSearchFilmsByTitleDirector() {
        Collection<Film> filmsSearchedByTitleDirector = filmRepository.getSearchFilms("DummyString", true, true);
    }
}