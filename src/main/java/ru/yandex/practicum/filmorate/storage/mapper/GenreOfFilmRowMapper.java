package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenreOfFilm;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class GenreOfFilmRowMapper implements RowMapper<GenreOfFilm> {
    @Override
    public GenreOfFilm mapRow(ResultSet rs, int rowNum) throws SQLException {
        int filmId = rs.getInt("film_Id");
        Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));

        return new GenreOfFilm(filmId, genre);
    }
}
