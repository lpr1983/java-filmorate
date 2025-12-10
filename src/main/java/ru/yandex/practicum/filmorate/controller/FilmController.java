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
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private Map<Integer, Film> films = new HashMap<>();
    private int filmCounter = 0;
    public static final LocalDate BIRHDAY_OF_CINEMA = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> all() {
        log.info("all");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film newObject) {
        log.info("create, input object {}", newObject);

        validate(newObject);

        int newId = getNextId();
        newObject.setId(newId);
        films.put(newId, newObject);

        log.info("create, output object {}", newObject);
        return newObject;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film filmToUpdate) {
        log.info("update, input object {}", filmToUpdate);

        validate(filmToUpdate);

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
        LocalDate releaseDate = film.getReleaseDate();
        if (releaseDate != null && releaseDate.isBefore(BIRHDAY_OF_CINEMA)) {
            String errorDescription = String.format("Дата релиза должна быть не раньше %s",
                    BIRHDAY_OF_CINEMA.format(DateTimeFormatter.ISO_LOCAL_DATE));
            throwValidateExceptionWithLogging(errorDescription);
        }
    }

    private void throwValidateExceptionWithLogging(String errorDescription) {
        ValidationException validationException = new ValidationException(errorDescription);
        log.error(errorDescription, validationException);
        throw validationException;
    }
}
