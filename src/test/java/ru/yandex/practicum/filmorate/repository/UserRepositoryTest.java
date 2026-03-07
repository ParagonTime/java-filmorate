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
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserRepository.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserRepositoryTest {
    private final UserRepository userRepository;
    private static User user;
    private static User friend;
    private static User other;
    private static int emailCount;

    public static String getNewEmail() {
        ++emailCount;
        return "newEmail" + emailCount + "@mail.mail";
    }

    @BeforeAll
    public static void start() {
        emailCount = 0;

        user = new User();
        user.setName("User");
        user.setLogin("Login");
        user.setEmail("email@mail.mail");
        user.setBirthday(LocalDate.now());

        friend = new User();
        friend.setName("Friend");
        friend.setLogin("LoginFriend");
        friend.setEmail("Friend@mail.mail");
        friend.setBirthday(LocalDate.now());

        other = new User();
        other.setName("Other");
        other.setLogin("LoginOther");
        other.setEmail("other@mail.mail");
        other.setBirthday(LocalDate.now());
    }

    @Test
    @Order(1)
    public void testFindUserById() {
        assertThrows(NotFoundException.class, () -> userRepository.getUser(100L));
    }

    @Test
    @Order(2)
    public void testAddUser() {
        assertThrows(NotFoundException.class, () -> userRepository.getUser(1L));
        assertEquals(1L, userRepository.save(user).getId());
    }

    @Test
    @Order(3)
    public void testAddUserAndPutNewFields() {
        user.setEmail(getNewEmail());
        Long idUser = userRepository.save(user).getId();
        assertEquals(2L, idUser);
        String newLogin = "newLogin";
        String newEmail = "newEmail";
        String newName = "newEmail@mail.mail";
        user.setId(idUser);
        user.setName(newName);
        user.setLogin(newLogin);
        user.setEmail(newEmail);
        User updatedUser = userRepository.update(user);
        assertEquals(newName, updatedUser.getName());
        assertEquals(newLogin, updatedUser.getLogin());
        assertEquals(newEmail, updatedUser.getEmail());
    }

    @Test
    @Order(4)
    public void testAddUsersAndAddFriend() {
        user.setEmail(getNewEmail());
        Long userOneId = userRepository.save(user).getId();
        user.setEmail(getNewEmail());
        Long userTwoId = userRepository.save(user).getId();
        Collection<User> friends = userRepository.addFriend(userOneId, userTwoId);
        assertEquals(2, friends.size());
    }

    @Test
    @Order(5)
    public void testAddUsersAndAddFriendAndDelete() {
        user.setEmail(getNewEmail());
        Long userOneId = userRepository.save(user).getId();
        user.setEmail(getNewEmail());
        Long userTwoId = userRepository.save(user).getId();
        Collection<User> friends = userRepository.addFriend(userOneId, userTwoId);
        assertEquals(2, friends.size());
        Collection<User> notFriend = userRepository.deleteFriend(userOneId, userTwoId);
        assertEquals(2, notFriend.size());
    }

    @Test
    @Order(6)
    public void testCommonFriends() {
        user.setEmail(getNewEmail());
        Long userOneId = userRepository.save(user).getId();
        user.setEmail(getNewEmail());
        Long userTwoId = userRepository.save(user).getId();
        user.setEmail(getNewEmail());
        Long userThreeId = userRepository.save(user).getId();
        Collection<User> friends = userRepository.addFriend(userOneId, userTwoId);
        assertEquals(2, friends.size());
        friends = userRepository.addFriend(userThreeId, userTwoId);
        assertEquals(2, friends.size());
        Collection<User> commonFriends = userRepository.getCommonFriends(userOneId, userThreeId);
        assertEquals(1, commonFriends.size());
    }
}