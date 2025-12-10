package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private Map<Integer, User> users = new HashMap<>();
    private int userCounter = 0;

    @GetMapping
    public Collection<User> all() {
        log.info("all");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User newUser) {
        log.info("create, input object: {}", newUser);

        int newId = getNextId();
        newUser.setId(newId);

        setNameField(newUser);

        users.put(newId, newUser);

        log.info("create, output object: {}", newUser);
        return newUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User userToUpdate) {
        log.info("update, input object: {}", userToUpdate);

        int id = userToUpdate.getId();
        User storedObject = users.get(id);
        if (storedObject == null) {
            String errorText = String.format("Не найден элемент с id=%d", id);
            NotFoundException notFoundException = new NotFoundException(errorText);
            log.error(errorText, notFoundException);
            throw notFoundException;
        }

        setNameField(userToUpdate);

        users.put(id, userToUpdate);

        log.info("update, output object: {}", userToUpdate);
        return userToUpdate;
    }

    private void setNameField(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private int getNextId() {
        userCounter++;
        return userCounter;
    }
}
