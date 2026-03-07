package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.GenreDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class GenreRowMapper implements RowMapper<GenreDto> {
    @Override
    public GenreDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        GenreDto genre = new GenreDto();
        genre.setId(rs.getLong("id"));
        genre.setName(rs.getString("genre_name"));
        return genre;
    }
}
