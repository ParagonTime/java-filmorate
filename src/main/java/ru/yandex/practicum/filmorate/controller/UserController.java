package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User postUsers(@Valid @RequestBody User user) {
        return userService.postUser(user);
    }

    @PutMapping
    public User putUsers(@Valid @RequestBody User newUser) {
        return userService.putUser(newUser);
    }

    @GetMapping
    public Collection<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") Long id) {
        return userService.getUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public Collection<User> addFriend(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public Collection<User> deleteFriend(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        return userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable("id") Long id) {
        return userService.getfriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable("id") Long id, @PathVariable("otherId") Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }
}
