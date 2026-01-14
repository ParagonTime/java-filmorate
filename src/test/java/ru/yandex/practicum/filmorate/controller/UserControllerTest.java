package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void shouldCreateUserSuccessfully() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Test Name");

        User createdUser = userController.postUsers(user);

        assertNotNull(createdUser.getId());
        assertEquals("test@mail.ru", createdUser.getEmail());
        assertEquals(1, userController.getUsers().size());
    }

    @Test
    void shouldSetLoginAsNameIfNameIsEmpty() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("onlyLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userController.postUsers(user);

        assertEquals("onlyLogin", createdUser.getName(), "Имя должно совпадать с логином, если оно пустое");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        User user = new User();
        user.setEmail("invalid-email.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.postUsers(user));
    }

    @Test
    void shouldThrowExceptionWhenBirthdayInFuture() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1)); // Завтра

        assertThrows(ValidationException.class, () -> userController.postUsers(user));
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        userController.postUsers(user);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("new@mail.ru");
        updatedUser.setLogin("newLogin");
        updatedUser.setBirthday(LocalDate.of(1990, 1, 1));

        userController.putUsers(updatedUser);

        User result = userController.getUsers().iterator().next();
        assertEquals("new@mail.ru", result.getEmail());
        assertEquals("newLogin", result.getLogin());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenIdIsIncorrect() {
        User user = new User();
        user.setId(999L);
        user.setEmail("test@mail.ru");
        user.setLogin("login");

        assertThrows(NotFoundException.class, () -> userController.putUsers(user));
    }
}
