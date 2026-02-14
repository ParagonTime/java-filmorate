package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class User {
    private Long id;
    @Email(message = "Электронная должна содержать символ @")
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Set<Long> friends;
//    private Set<Long> likedFilms;
}
