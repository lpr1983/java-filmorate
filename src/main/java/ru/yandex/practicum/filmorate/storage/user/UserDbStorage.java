package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DbStorageException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final NamedParameterJdbcTemplate jdbc;
    private final RowMapper<User> mapper;

    @Override
    public List<User> getAll() {
        String getAllQuery = "SELECT * FROM users;";
        Map<String, Object> params = Map.of();

        return jdbc.query(getAllQuery, params, mapper);
    }

    @Override
    public Optional<User> getById(int userId) {
        Optional<User> result;

        String getByIdQuery = """
                SELECT * FROM users
                WHERE id = :userId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        try {
            User user = jdbc.queryForObject(getByIdQuery, params, mapper);
            result = Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ex) {
            result = Optional.empty();
        }

        return result;
    }

    @Override
    public User create(User user) {
        String createQuery = """
                INSERT INTO users(email, login, name, birthday)
                VALUES (:email, :login, :name, :birthday);
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", java.sql.Date.valueOf(user.getBirthday()));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(createQuery, params, keyHolder);

        Integer createdUserId = keyHolder.getKeyAs(Integer.class);
        if (createdUserId == null) {
            throw new DbStorageException("Не удалось сохранить данные");
        }
        user.setId(createdUserId);

        return user;
    }

    @Override
    public User update(User user) {
        Integer userId = user.getId();
        String updateQuery = """
                UPDATE users SET
                email = :email,
                login = :login,
                name  = :name,
                birthday = :birthday
                WHERE id = :userId;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", java.sql.Date.valueOf(user.getBirthday()))
                .addValue("userId", userId);

        int rowsUpdated = jdbc.update(updateQuery, params);
        if (rowsUpdated == 0) {
            throw new DbStorageException("Не удалось обновить данные");
        }

        return getById(userId)
                .orElseThrow(() -> new DbStorageException("После обновления не найден пользователь с id:" + userId));
    }

    @Override
    public void delete(int userId) {
        String deleteByIdQuery = """
                DELETE FROM users
                WHERE id = :userId;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        jdbc.update(deleteByIdQuery, params);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        String addFriendQuery = """
                INSERT INTO friends(user_id, friend_id)
                VALUES (:userId, :friendId);
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        try {
            jdbc.update(addFriendQuery, params);
        } catch (DuplicateKeyException ex) {
            // Уже добавлен в друзья - ничего не делать
        }
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        String deleteFromFriendsQuery = """
                DELETE FROM friends
                WHERE user_id = :userId
                AND friend_id = :friendId;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        jdbc.update(deleteFromFriendsQuery, params);
    }

    @Override
    public List<User> getFriends(int userId) {
        String getFriendsQuery = """
                SELECT users.* from friends
                JOIN users ON users.id = friends.friend_id
                WHERE friends.user_id = :userId;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        return jdbc.query(getFriendsQuery, params, mapper);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        String commonFriendsQuery = """
                SELECT * from users
                JOIN (
                SELECT f1.friend_id FROM friends f1
                JOIN friends f2 ON f1.friend_id = f2.friend_id
                WHERE f1.user_id = :userId AND f2.user_id = :otherId
                ) common_friends
                ON users.id = common_friends.friend_id
                ;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("otherId", otherId);

        return jdbc.query(commonFriendsQuery, params, mapper);
    }
}
