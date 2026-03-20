package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateDirectorRequest {
    @NotNull(message = "Id режиссёра должен присутствовать")
    @Positive(message = "Id должен быть положительным числом")
    private Long id;
    @NotBlank(message = "Имя режиссёра не может быть пустым")
    private String name;

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }
}
