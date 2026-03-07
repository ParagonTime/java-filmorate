package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.MpaDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRowMapper implements RowMapper<MpaDto> {
    @Override
    public MpaDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        MpaDto mpa = new MpaDto();
        mpa.setId(rs.getLong("id"));
        mpa.setName(rs.getString("rating_name"));
        return mpa;
    }
}
