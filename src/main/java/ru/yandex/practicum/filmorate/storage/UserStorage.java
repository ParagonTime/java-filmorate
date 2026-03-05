package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User save(User user);

    User update(User user);

    Collection<User> getUsers();

    User getUser(Long id);

    Collection<User> addFriend(Long id, Long friendId);

    Collection<User> deleteFriend(Long id, Long friendId);

    Collection<User> getFriends(Long id);

    Collection<User> getCommonFriends(Long id, Long otherId);

    boolean userExist(Long id);
}
