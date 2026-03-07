package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.MpaRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaRepository mpaRepository;

    public Collection<MpaDto> getAllMpa() {
        return mpaRepository.getAllMpa();
    }

    public MpaDto getMpaById(Long id) {
        return mpaRepository.getMpaById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с таким id не найден"));
    }

    public MpaDto getMpaByFilmId(long filmId) {
        return mpaRepository.getMpaByFilmId(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с таким id не найден"));
    }
}
