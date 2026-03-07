package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateFilmRequest {
    @NotNull
    private Long id;
    private String name;
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;
    private MpaDto mpa;
    private List<GenreDto> genres;

    public boolean hasId() {
        return !(id == null || id < 0);
    }

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return !(releaseDate == null || releaseDate.isAfter(LocalDate.now()));
    }

    public boolean hasDuration() {
        return !(duration == null || duration <= 0);
    }

    public boolean hasMpa() {
        return !(mpa == null);
    }

    public boolean hasGenres() {
        return !(genres == null || genres.isEmpty());
    }
}
