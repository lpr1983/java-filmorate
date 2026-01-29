package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ru.yandex.practicum.filmorate.exception.DbStorageException;

import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
public class BaseDbStorage<T> {
    protected final NamedParameterJdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    protected Optional<T> getOne(String query, MapSqlParameterSource params) {
        Optional<T> result;
        try {
            T t = jdbc.queryForObject(query, params, mapper);
            result = Optional.ofNullable(t);
        } catch (EmptyResultDataAccessException ex) {
            result = Optional.empty();
        }
        return result;
    }

    protected Optional<T> getOneById(String query, int id) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        return getOne(query, params);
    }

    protected int insertWithKeyReturning(String query, MapSqlParameterSource params) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(query, params, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id == null) {
            throw new DbStorageException("Не удалось сохранить данные");
        }
        return id;
    }

    protected void updateWithCheckResult(String query, MapSqlParameterSource params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new DbStorageException("Не удалось обновить данные");
        }
    }

    protected java.sql.Date normaliseDateForSql(LocalDate date) {
        java.sql.Date birthdaySql = null;
        if (date != null) {
            birthdaySql = java.sql.Date.valueOf(date);
        }
        return birthdaySql;
    }

}
