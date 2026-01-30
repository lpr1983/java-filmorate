package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage extends BaseDbStorage<MpaRating> {
    public MpaDbStorage(NamedParameterJdbcTemplate jdbc, RowMapper<MpaRating> mapper) {
        super(jdbc, mapper);
    }

    public List<MpaRating> getAll() {
        return jdbc.query("SELECT * from mpa_ratings", mapper);
    }

    public Optional<MpaRating> getById(int id) {
        String query = """
                SELECT * from mpa_ratings
                WHERE id = :id
                """;
        return getOneById(query, id);
    }

}
