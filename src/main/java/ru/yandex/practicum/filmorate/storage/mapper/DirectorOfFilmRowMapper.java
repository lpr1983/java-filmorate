package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.film.DirectorOfFilm;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DirectorOfFilmRowMapper implements RowMapper<DirectorOfFilm> {
    @Override
    public DirectorOfFilm mapRow(ResultSet rs, int rowNum) throws SQLException {
        int filmId = rs.getInt("film_Id");
        Director director = new Director(rs.getInt("id"), rs.getString("name"));

        return new DirectorOfFilm(filmId, director);
    }
}
