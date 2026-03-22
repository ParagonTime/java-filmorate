package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;
    private String name;
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;
    private Long ratingId;
    private Set<Long> usersLiked = new HashSet<>();
    private Set<Long> genresIds = new HashSet<>();
    private Set<Long> directorsIds = new HashSet<>();

    public void setGenresIds(Set<Long> collect) {
        this.genresIds = new HashSet<>(collect);
    }

    public void setDirectorsIds(Set<Long> collect) {
        this.directorsIds = new HashSet<>(collect);
    }
}
