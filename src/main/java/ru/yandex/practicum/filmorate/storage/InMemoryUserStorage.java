package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private static final String EMPTY_EMAIL_EXCEPTION_MESSAGE = "Электронная почта не может быть пустой";
    private static final String EMPTY_ID_EXCEPTION_MESSAGE = "ID не может быть пустой";
    private static final String BLANK_LOGIN_EXCEPTION_MESSAGE = "Логин не может быть пустым и содержать пробелы";
    private static final String FUTURE_BIRTHDAY_EXCEPTION_MESSAGE = "Дата рождения не может быть в будущем";
    private static final String USER_NO_FOUND_MESSAGE = "Пользователь с таким ID не найден";

    private final Map<Long, User> users;

    public InMemoryUserStorage() {
        this.users = new HashMap<>();
    }

    @Override
    public User postUser(User user) {
        validateUser(user);
        user.setId(nextId());
        user.setFriends(new HashSet<>());
        users.put(user.getId(), user);
        log.debug("New user {}", user);
        return user;
    }

    @Override
    public User putUser(User newUser) {
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

    @Override
    public Collection<User> getUsers() {
        log.debug("Call getUsers with {}", users.values());
        return users.values();
    }

    @Override
    public User getUser(Long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        }
        throw new NotFoundException(USER_NO_FOUND_MESSAGE);
    }

    @Override
    public Collection<User> addFriend(Long id, Long friendId) {
        if (users.containsKey(id) && users.containsKey(friendId)) {
            users.get(id).getFriends().add(friendId);
            users.get(friendId).getFriends().add(id);
            return List.of(users.get(id), users.get(friendId));
        }
        throw new NotFoundException(USER_NO_FOUND_MESSAGE);
    }

    @Override
    public Collection<User> deleteFriend(Long id, Long friendId) {
        if (users.containsKey(id) && users.containsKey(friendId)) {
            users.get(id).getFriends().remove(friendId);
            users.get(friendId).getFriends().remove(id);
            return List.of(users.get(id), users.get(friendId));
        }
        throw new NotFoundException(USER_NO_FOUND_MESSAGE);
    }

    @Override
    public Collection<User> getFriends(Long id) {
        if (users.containsKey(id)) {
            Set<Long> friendsIds = users.get(id).getFriends();
            return friendsIds.stream()
                    .map(users::get)
                    .toList();
        }
        throw new NotFoundException(USER_NO_FOUND_MESSAGE);
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) {
        if (users.containsKey(id) && users.containsKey(otherId)) {
            User user = users.get(id);
            User otherUser = users.get(otherId);
            return user.getFriends().stream()
                    .filter(otherUser.getFriends()::contains)
                    .map(users::get)
                    .toList();
        }
        throw new NotFoundException(USER_NO_FOUND_MESSAGE);
    }

    @Override
    public boolean userExist(Long id) {
        return users.containsKey(id);
    }

    private void validateUser(User user) {
        if (user == null || user.getEmail() == null) {
            log.warn(EMPTY_EMAIL_EXCEPTION_MESSAGE);
            throw new ValidationException(EMPTY_EMAIL_EXCEPTION_MESSAGE);
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn(BLANK_LOGIN_EXCEPTION_MESSAGE);
            throw new ValidationException(BLANK_LOGIN_EXCEPTION_MESSAGE);
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.warn(FUTURE_BIRTHDAY_EXCEPTION_MESSAGE);
            throw new ValidationException(FUTURE_BIRTHDAY_EXCEPTION_MESSAGE);
        }
    }

    private long nextId() {
        return users.size() + 1;
    }

}
