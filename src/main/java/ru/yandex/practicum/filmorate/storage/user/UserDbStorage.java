package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository("userDbStorage")
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<User> getAll() {
        return List.of();
    }

    @Override
    public Optional<User> getById(int userId) {
        return Optional.empty();
    }

    @Override
    public User create(User user) {
        int id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday())
        );
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        return null;
    }

    @Override
    public void delete(int userId) {
        // It is not implemented
    }

    @Override
    public void addFriend(int userId, int friendId) {
        // It is not implemented
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        // It is not implemented
    }

    @Override
    public List<User> getFriends(int userId) {
        return List.of();
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        return List.of();
    }
}
