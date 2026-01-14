package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Long id;
    @Email(message = "Электронная должна содержать символ @")
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
