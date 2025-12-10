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
    void testReleaseDateValidation() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Desc");
        film.setDuration(100);

        film.setReleaseDate(FilmController.BIRHDAY_OF_CINEMA.minusDays(1));
        assertThrows(ValidationException.class, () -> controller.create(film));

        film.setReleaseDate(FilmController.BIRHDAY_OF_CINEMA);
        assertDoesNotThrow(() -> controller.create(film));

        film.setReleaseDate(LocalDate.now().plusDays(1));
        assertDoesNotThrow(() -> controller.create(film)); // по твоей текущей логике проверка будущей даты не реализована
    }

}