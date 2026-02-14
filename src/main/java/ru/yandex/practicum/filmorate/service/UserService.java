package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User postUser(@Valid User user) {
        return userStorage.postUser(user);
    }

    public User putUser(@Valid User newUser) {
        return userStorage.putUser(newUser);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUser(Long id) {
        return userStorage.getUser(id);
    }

    public Collection<User> addFriend(Long id, Long friendId) {
        return userStorage.addFriend(id, friendId);
    }

    public Collection<User> deleteFriend(Long id, Long friendId) {
        return userStorage.deleteFriend(id, friendId);
    }

    public Collection<User> getfriends(Long id) {
        return userStorage.getFriends(id);
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    public boolean userExist(Long id) {
        return userStorage.userExist(id);
    }
}
