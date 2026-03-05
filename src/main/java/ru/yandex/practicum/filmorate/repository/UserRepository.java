package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Repository
public class UserRepository extends BaseRepository<User> implements UserStorage {

    private static final String INSERT_QUERY = "INSERT INTO users(name, login, email, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET name = ?, login = ?, email = ?," +
            "birthday = ? WHERE id = ?";
    private static final String FIND_ALL_USERS = "SELECT * FROM users";
    private static final String FIND_USER_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String INSERT_FRIEND = "INSERT INTO friendship(friend_from, friend_to, friendship_status)" +
            "VALUES (?, ?, ?)";
    private static final String DELETE_FRIEND = "DELETE FROM friendship WHERE friend_from = ? AND friend_to = ?";
    private static final String FIND_FRIENDS = "SELECT u.* FROM users u JOIN friendship f ON u.id = f.friend_to " +
            "WHERE f.friend_from = ?";
    private static final String FIND_COMMON_FRIENDS = "SELECT u.* FROM users u " +
            "JOIN friendship f1 ON u.id = f1.friend_to AND f1.friend_from = ? " +
            "JOIN friendship f2 ON u.id = f2.friend_to AND f2.friend_from = ?";

    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User save(User user) {
        Long id = insert(
                INSERT_QUERY,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        update(
                UPDATE_QUERY,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public Collection<User> getUsers() {
        return findMany(FIND_ALL_USERS);
    }

    @Override
    public User getUser(Long id) {
        return findOne(FIND_USER_BY_ID, id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    @Override
    public Collection<User> addFriend(Long id, Long friendId) {
        System.out.println("Adding friend: " + id + " -> " + friendId);
        User user = getUser(id);
        User friend = getUser(friendId);
        update(
                INSERT_FRIEND,
                id,
                friendId,
                FriendshipStatus.UNCONFIRMED.name()
        );
        return List.of(user, friend);
    }

    @Override
    public Collection<User> deleteFriend(Long id, Long friendId) {
        User user = getUser(id);
        User friend = getUser(friendId);
        update(DELETE_FRIEND, id, friendId);
//        update(DELETE_FRIEND, friendId, id); для тестов

        return List.of(user, friend);
    }

    @Override
    public Collection<User> getFriends(Long id) {
        User user = getUser(id);
        return findMany(FIND_FRIENDS, id);
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) {
        User user = getUser(id);
        User friend = getUser(otherId);
        return findMany(FIND_COMMON_FRIENDS, id, otherId);
    }

    @Override
    public boolean userExist(Long id) {
        User user = getUser(id);
        return user != null;
    }
}
