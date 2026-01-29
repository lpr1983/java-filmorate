package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreDbStorage extends BaseDbStorage<Genre> {
    public GenreDbStorage(NamedParameterJdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public List<Genre> getAll() {
        return jdbc.query("SELECT * from genres", mapper);
    }

    public Optional<Genre> getById(int id) {
        String query = """
                SELECT * from genres
                WHERE id = :id
                """;
        return getOneById(query, id);
    }

    public List<Genre> getGenresByIds(List<Integer> ids) {
        if (ids.isEmpty())
            return List.of();

        String query = """
                SELECT * from genres
                WHERE id in (:ids)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ids", ids);

        return jdbc.query(query, params, mapper);
    }
}
