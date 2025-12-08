package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setup() {
        controller = new UserController();
    }

    @Test
    void testValidUserCreation() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertDoesNotThrow(() -> controller.create(user));
    }

    @Test
    void testEmailValidation() {
        User user = new User();
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        user.setEmail(null);
        assertThrows(ValidationException.class, () -> controller.create(user));

        user.setEmail("");
        assertThrows(ValidationException.class, () -> controller.create(user));

        user.setEmail("without-a-dog");
        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void testLoginValidation() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        user.setLogin(null);
        assertThrows(ValidationException.class, () -> controller.create(user));

        user.setLogin("");
        assertThrows(ValidationException.class, () -> controller.create(user));

        user.setLogin("with space");
        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void testBirthdayValidation() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("login");

        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> controller.create(user));

        user.setBirthday(LocalDate.of(2000, 1, 1));
        assertDoesNotThrow(() -> controller.create(user));
    }

    @Test
    void testNameDefaultsToLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("");

        User created = controller.create(user);
        assertEquals("login", created.getName());
    }
}
