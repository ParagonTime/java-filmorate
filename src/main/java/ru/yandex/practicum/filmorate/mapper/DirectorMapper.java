package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DirectorMapper {

    public DirectorDto mapToDirector(NewDirectorRequest newDirector) {
        DirectorDto director = new DirectorDto();
        director.setName(newDirector.getName());
        return director;
    }

    public DirectorDto updateDirector(DirectorDto director, UpdateDirectorRequest request) {
        if (request.hasName()) {
            director.setName(request.getName());
        }
        return director;
    }
}
