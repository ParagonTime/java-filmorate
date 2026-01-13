package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User postUsers(@RequestBody User user) {

        return null;
    }

    @PutMapping
    public User putUsers(@RequestBody User newUser) {

        return null;
    }

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }
}
