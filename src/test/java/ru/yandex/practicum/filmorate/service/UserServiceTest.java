package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
    private final UserService userService;
    private static NewUserRequest newUser;
    private static NewUserRequest friend;
    private static NewUserRequest other;
    private static int emailCount;

    public static String getNewEmail() {
        ++emailCount;
        return "email" + emailCount + "@mail.mail";
    }

    @BeforeAll
    public static void start() {
        emailCount = 0;

        newUser = new NewUserRequest();
        newUser.setName("User");
        newUser.setLogin("Login");
        newUser.setEmail(getNewEmail());
        newUser.setBirthday(LocalDate.now());

        friend = new NewUserRequest();
        friend.setName("Friend");
        friend.setLogin("LoginFriend");
        friend.setEmail(getNewEmail());
        friend.setBirthday(LocalDate.now());

        other = new NewUserRequest();
        other.setName("Other");
        other.setLogin("LoginOther");
        other.setEmail(getNewEmail());
        other.setBirthday(LocalDate.now());
    }

    @Test
    @Order(1)
    public void testGetUserByNotExistId() {
        assertThrows(NotFoundException.class, () -> userService.getUser(999L));
    }

    @Test
    @Order(2)
    public void testPostUser() {
        UserDto createdUser = userService.postUser(newUser);
        assertNotNull(createdUser.getId());
        assertEquals(newUser.getName(), createdUser.getName());
        assertEquals(newUser.getLogin(), createdUser.getLogin());
        assertEquals(newUser.getEmail(), createdUser.getEmail());
        assertEquals(newUser.getBirthday(), createdUser.getBirthday());
    }

    @Test
    @Order(3)
    public void testPutUser() {
        newUser.setEmail(getNewEmail());
        UserDto createdUser = userService.postUser(newUser);

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setId(createdUser.getId());
        updateRequest.setName("NewName");
        updateRequest.setLogin("NewLogin");
        updateRequest.setEmail(getNewEmail());
        updateRequest.setBirthday(LocalDate.now());

        UserDto updatedUser = userService.putUser(updateRequest);
        assertEquals(updateRequest.getId(), updatedUser.getId());
        assertEquals(updateRequest.getName(), updatedUser.getName());
        assertEquals(updateRequest.getLogin(), updatedUser.getLogin());
        assertEquals(updateRequest.getEmail(), updatedUser.getEmail());
        assertEquals(updateRequest.getBirthday(), updatedUser.getBirthday());
    }

    @Test
    @Order(4)
    public void testGetUser() {
        newUser.setEmail(getNewEmail());
        UserDto createdUser = userService.postUser(newUser);
        UserDto foundUser = userService.getUser(createdUser.getId());
        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals(createdUser.getName(), foundUser.getName());
    }

    @Test
    @Order(5)
    public void testGetUsers() {
        newUser.setEmail(getNewEmail());
        userService.postUser(newUser);
        userService.postUser(friend);

        Collection<UserDto> users = userService.getUsers();
        assertTrue(users.size() >= 2);
    }

    @Test
    @Order(6)
    public void testAddFriend() {
        newUser.setEmail(getNewEmail());
        UserDto user1 = userService.postUser(newUser);
        friend.setEmail(getNewEmail());
        UserDto user2 = userService.postUser(friend);

        Collection<UserDto> friends = userService.addFriend(user1.getId(), user2.getId());
        assertEquals(2, friends.size());

        Collection<UserDto> user1Friends = userService.getFriends(user1.getId());
        assertEquals(1, user1Friends.size());
        assertEquals(user2.getId(), user1Friends.iterator().next().getId());
    }

    @Test
    @Order(7)
    public void testDeleteFriend() {
        newUser.setEmail(getNewEmail());
        UserDto user1 = userService.postUser(newUser);
        friend.setEmail(getNewEmail());
        UserDto user2 = userService.postUser(friend);

        userService.addFriend(user1.getId(), user2.getId());
        Collection<UserDto> user1Friends = userService.getFriends(user1.getId());
        assertEquals(1, user1Friends.size());

        Collection<UserDto> deletedFriends = userService.deleteFriend(user1.getId(), user2.getId());
        assertEquals(2, deletedFriends.size());

        Collection<UserDto> user1FriendsAfterDelete = userService.getFriends(user1.getId());
        assertEquals(0, user1FriendsAfterDelete.size());
    }

    @Test
    @Order(8)
    public void testGetCommonFriends() {
        newUser.setEmail(getNewEmail());
        UserDto user1 = userService.postUser(newUser);
        friend.setEmail(getNewEmail());
        UserDto user2 = userService.postUser(friend);
        other.setEmail(getNewEmail());
        UserDto user3 = userService.postUser(other);

        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user3.getId(), user2.getId());

        Collection<UserDto> commonFriends = userService.getCommonFriends(user1.getId(), user3.getId());
        assertEquals(1, commonFriends.size());
        assertEquals(user2.getId(), commonFriends.iterator().next().getId());
    }
}