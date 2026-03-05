package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User postUser(User user) {
        log.debug("create user {}", user);
        return userStorage.postUser(user);
    }

    public User putUser(User newUser) {
        log.debug("put user {}", newUser);
        return userStorage.putUser(newUser);
    }

    public Collection<User> getUsers() {
        log.debug("call getUsers");
        return userStorage.getUsers();
    }

    public User getUser(Long id) {
        log.debug("call getUser by id {}", id);
        return userStorage.getUser(id);
    }

    public Collection<User> addFriend(Long id, Long friendId) {
        log.debug("add friend {} user {}", friendId, id);
        return userStorage.addFriend(id, friendId);
    }

    public Collection<User> deleteFriend(Long id, Long friendId) {
        log.debug("delete friend {} user {}", friendId, id);
        return userStorage.deleteFriend(id, friendId);
    }

    public Collection<User> getfriends(Long id) {
        log.debug("call getFriends user {}", id);
        return userStorage.getFriends(id);
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        log.debug("call getCommonFriends user {} other {}", id, otherId);
        return userStorage.getCommonFriends(id, otherId);
    }

    public boolean userExist(Long id) {
        return userStorage.userExist(id);
    }
}
