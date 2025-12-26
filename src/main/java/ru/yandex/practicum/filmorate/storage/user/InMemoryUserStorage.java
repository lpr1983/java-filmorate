package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Set<Integer>> friends = new HashMap<>();
    private int nextId = 0;

    @Override
    public List<User> getAll() {
        return users.values().stream().toList();
    }

    @Override
    public Optional<User> getById(int userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public User create(User newUser) {
        int newId = getNextId();
        newUser.setId(newId);
        users.put(newId, newUser);
        return newUser;
    }

    @Override
    public User update(User userToUpdate) {
        int id = userToUpdate.getId();
        users.put(id, userToUpdate);
        return userToUpdate;
    }

    @Override
    public void delete(int userId) {
        users.remove(userId);
        friends.remove(userId);
        friends.values().forEach(friendsOfUser -> friendsOfUser.remove(userId));
    }

    @Override
    public void addFriend(int userId, int friendId) {
        Set<Integer> friendsOfUser = friends.get(userId);
        if (friendsOfUser == null) {
            friendsOfUser = new HashSet<>();
        }
        friendsOfUser.add(friendId);
        friends.put(userId, friendsOfUser);

        Set<Integer> friendsOfFriend = friends.get(friendId);
        if (friendsOfFriend == null) {
            friendsOfFriend = new HashSet<>();
        }
        friendsOfFriend.add(userId);
        friends.put(friendId, friendsOfFriend);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        Set<Integer> friendsOfUser = friends.get(userId);
        if (friendsOfUser != null) {
            friendsOfUser.remove(friendId);
        }

        Set<Integer> friendsOfFriend = friends.get(friendId);
        if (friendsOfFriend != null) {
            friendsOfFriend.remove(userId);
        }
    }

    @Override
    public List<User> getFriends(int userId) {
        Set<Integer> friendsOfUser = friends.getOrDefault(userId, Set.of());
        return friendsOfUser.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        Set<Integer> friendsId1 = friends.getOrDefault(userId, Set.of());
        Set<Integer> friendsId2 = friends.getOrDefault(otherId, Set.of());

        return friendsId1.stream()
                .filter(friendsId2::contains)
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private int getNextId() {
        nextId++;
        return nextId;
    }
}
