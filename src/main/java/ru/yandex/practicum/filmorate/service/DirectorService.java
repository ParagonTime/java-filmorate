package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.repository.DirectorRepository;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorRepository directorRepository;
    private final DirectorMapper directorMapper;

    public DirectorDto createDirector(NewDirectorRequest newDirector) {
        DirectorDto director = directorRepository.save(directorMapper.mapToDirector(newDirector));
        log.debug("created new director id: {}, name: {}", director.getId(), director.getName());
        return director;
    }

    public DirectorDto updateDirector(UpdateDirectorRequest request) {
        DirectorDto director = directorRepository.getDirector(request.getId());
        DirectorDto update = directorMapper.updateDirector(director, request);
        DirectorDto newDirector = directorRepository.update(update);
        log.debug("updated director: id {}, name {}", newDirector.getId(), newDirector.getName());
        return newDirector;
    }

    public Collection<DirectorDto> getDirectors() {
        log.debug("call get directors");
        return directorRepository.getDirectors();
    }

    public DirectorDto getDirector(Long id) {
        log.debug("call get director wih id {}", id);
        return directorRepository.getDirector(id);
    }

    public Boolean deleteDirector(Long id) {
        log.debug("delete director with id {}", id);
        return directorRepository.deleteDirector(id);
    }
}
