package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getInt("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getObject("duration", Integer.class));

        Integer mpaId = resultSet.getObject("mpa_rating_id", Integer.class);
        if (mpaId != null) {
            MpaRating mpa = new MpaRating(
                    mpaId,
                    resultSet.getString("mpa_name"),
                    resultSet.getObject("mpa_age", Integer.class)
            );
            film.setMpa(mpa);
        }

        return film;
    }
}