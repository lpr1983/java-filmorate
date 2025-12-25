package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setup() {
        //UserService userService = new UserService();
        //controller = new UserController(userService);
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
