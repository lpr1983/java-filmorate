package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private Map<Integer, Film> films = new HashMap<>();
    private int filmCounter = 0;

    @GetMapping
    public Collection<Film> all() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film newObject) {
        log.info("create, input object {}", newObject);

        try {
            validate(newObject);
        } catch (ValidationException exception) {
            log.error(exception.getMessage(), exception);
            throw exception;
        }

        int newId = getNextId();
        newObject.setId(newId);
        films.put(newId, newObject);

        log.info("create, output object {}", newObject);
        return newObject;
    }

    @PutMapping
    public Film update(@RequestBody Film filmToUpdate) {
        log.info("update, input object {}", filmToUpdate);

        try {
            validate(filmToUpdate);
        } catch (ValidationException exception) {
            log.error(exception.getMessage(), exception);
            throw exception;
        }

        int id = filmToUpdate.getId();
        Film storedObject = films.get(id);
        if (storedObject == null) {
            String errorText = String.format("Не найден элемент с id=%d", id);
            NotFoundException notFoundException = new NotFoundException(errorText);
            log.error(errorText, notFoundException);
            throw notFoundException;
        }

        films.put(id, filmToUpdate);
        log.info("output object: {}", filmToUpdate);
        return filmToUpdate;
    }

    private int getNextId() {
        filmCounter++;
        return filmCounter;
    }

    private void validate(Film film) {
        String filmName = film.getName();
        if (filmName == null || filmName.isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }

        String filmDescription = film.getDescription();
        if (filmDescription != null && filmDescription.length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        LocalDate releaseDate = film.getReleaseDate();
        if (releaseDate != null && releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза должны быть не раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
