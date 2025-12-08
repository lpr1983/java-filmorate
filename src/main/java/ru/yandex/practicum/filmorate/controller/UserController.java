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
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
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
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User newUser) {
        log.info("create, input object: {}", newUser);

        try {
            validate(newUser);
        } catch (ValidationException exception) {
            log.error(exception.getMessage(), exception);
            throw exception;
        }

        int newId = getNextId();
        newUser.setId(newId);

        setNameField(newUser);

        users.put(newId, newUser);

        log.info("output object: {}", newUser);
        return newUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User userToUpdate) {
        log.info("update, input object: {}", userToUpdate);

        try {
            validate(userToUpdate);
        } catch (ValidationException exception) {
            log.error(exception.getMessage(), exception);
            throw exception;
        }

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

        log.info("output object: {}", userToUpdate);
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

    private void validate(User userToValidate) {
        String email = userToValidate.getEmail();
        if (email == null || email.isBlank() || !email.contains("@")
        ) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }

        String login = userToValidate.getLogin();
        if (login == null || login.isBlank() || login.contains(" ")
        ) {
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }

        LocalDate birthday = userToValidate.getBirthday();
        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
