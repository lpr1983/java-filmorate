package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setup() {
        controller = new FilmController();
    }

    @Test
    void testValidFilmCreation() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    void testNameCannotBeEmpty() {
        Film film = new Film();
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        film.setName(null);
        assertThrows(ValidationException.class, () -> controller.create(film));

        film.setName("");
        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void testDescriptionLength() {
        Film film = new Film();
        film.setName("Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        film.setDescription("x".repeat(201));
        assertThrows(ValidationException.class, () -> controller.create(film));

        film.setDescription("x".repeat(200));
        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    void testReleaseDateValidation() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Desc");
        film.setDuration(100);

        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> controller.create(film));

        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        assertDoesNotThrow(() -> controller.create(film));

        film.setReleaseDate(LocalDate.now().plusDays(1));
        assertDoesNotThrow(() -> controller.create(film)); // по твоей текущей логике проверка будущей даты не реализована
    }

    @Test
    void testDurationValidation() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        film.setDuration(0);
        assertThrows(ValidationException.class, () -> controller.create(film));

        film.setDuration(-5);
        assertThrows(ValidationException.class, () -> controller.create(film));

        film.setDuration(10);
        assertDoesNotThrow(() -> controller.create(film));
    }
}