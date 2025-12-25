package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> all() {
        return userStorage.getAll();
    }

    public User getById(int id) {
        return userStorage.getById(id).
                orElseThrow(() -> new NotFoundException("Не найден пользователь с id: " + id));
    }

    public User create(User newUser) {
        log.info("Create, input object: {}", newUser);

        processNameField(newUser);

        User createdUser = userStorage.create(newUser);

        log.info("Create, output object: {}", createdUser);
        return createdUser;
    }

    public User update(User userToUpdate) {
        log.info("Update, input object: {}", userToUpdate);

        int id = userToUpdate.getId();
        if (userStorage.getById(id).isEmpty()) {
            throw new NotFoundException(String.format("Не найден элемент с id=%d", id));
        }

        processNameField(userToUpdate);

        User updatedUser = userStorage.update(userToUpdate);

        log.info("Update, output object: {}", updatedUser);
        return updatedUser;
    }

    public void deleteById(int id) {
        checkUserExists(id);
        userStorage.delete(id);

        log.info("User deleted: id={}", id);
    }

    public void addFriend(int userId, int friendId) {
        checkUsers(userId, friendId);
        userStorage.addFriend(userId, friendId);

        log.info("Friend added: userId={}, friendId={}", userId, friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        checkUsers(userId, friendId);
        userStorage.deleteFriend(userId, friendId);

        log.info("Friend deleted: userId={}, friendId={}", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        checkUserExists(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        log.debug("Getting common friends for userId={} and otherId={}", userId, otherId);

        checkUsers(userId, otherId);

        Set<User> friends1 = new HashSet<>(userStorage.getFriends(userId));
        Set<User> friends2 = new HashSet<>(userStorage.getFriends(otherId));

        return friends1.stream().
                filter(friends2::contains).toList();
    }

    private void checkUsers(int userId, int friendId) {
        if (userId == friendId) {
            throw new ValidationException("Пользователь не может быть другом самого себя.");
        }
        checkUserExists(userId);
        checkUserExists(friendId);
    }

    private void checkUserExists(int userId) {
        userStorage.getById(userId).
                orElseThrow(() -> new NotFoundException("Не найден пользователь с id:" + userId));
    }

    private void processNameField(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
