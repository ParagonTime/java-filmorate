package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.DirectorDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DirectorRowMapper implements RowMapper<DirectorDto> {
    @Override
    public DirectorDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        DirectorDto director = new DirectorDto();
        director.setId(rs.getLong("id"));
        director.setName(rs.getString("name"));
        return director;
    }
}
