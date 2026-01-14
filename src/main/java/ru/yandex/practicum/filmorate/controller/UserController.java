package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private static final String EMPTY_EMAIL_EXCEPTION_MESSAGE = "Электронная почта не может быть пустой";
    private static final String EMPTY_ID_EXCEPTION_MESSAGE = "ID не может быть пустой";
    private static final String BLANK_LOGIN_EXCEPTION_MESSAGE = "Логин не может быть пустым и содержать пробелы";
    private static final String FUTURE_BIRTHDAY_EXCEPTION_MESSAGE = "Дата рождения не может быть в будущем";
    private static final String USER_NO_FOUND_MESSAGE = "Пользователь с таким ID не найден";

    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User postUsers(@Valid @RequestBody User user) {
        validateUser(user);
        user.setId(nextId());
        users.put(user.getId(), user);
        log.debug("New user {}", user);
        return user;
    }

    @PutMapping
    public User putUsers(@Valid @RequestBody User newUser) {
        if (newUser == null || newUser.getId() == null) {
            log.warn(EMPTY_ID_EXCEPTION_MESSAGE);
            throw new ValidationException(EMPTY_ID_EXCEPTION_MESSAGE);
        }
        User currentUser = users.get(newUser.getId());
        if (currentUser == null) {
            log.warn(USER_NO_FOUND_MESSAGE);
            throw new NotFoundException(USER_NO_FOUND_MESSAGE);
        }
        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            currentUser.setName(newUser.getName());
        }
        if (newUser.getLogin() != null) {
            currentUser.setLogin(newUser.getLogin());
        }
        if (newUser.getEmail() != null && newUser.getEmail().contains("@")) {
            currentUser.setEmail(newUser.getEmail());
        }
        if (newUser.getBirthday() != null && !newUser.getBirthday().isAfter(LocalDate.now())) {
            currentUser.setBirthday(newUser.getBirthday());
        }
        log.debug("Put user {}", currentUser);
        return currentUser;
    }

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    private void validateUser(User user) {
        try {
            if (user == null || user.getEmail() == null) {
                throw new ValidationException(EMPTY_EMAIL_EXCEPTION_MESSAGE);
            }
            if (user.getLogin() == null || user.getLogin().isBlank()) {
                throw new ValidationException(BLANK_LOGIN_EXCEPTION_MESSAGE);
            }
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
                throw new ValidationException(FUTURE_BIRTHDAY_EXCEPTION_MESSAGE);
            }
        } catch (RuntimeException e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    private long nextId() {
        return users.size() + 1;
    }
}
