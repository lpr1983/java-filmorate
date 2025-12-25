package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    public static final LocalDate BIRTHDAY_OF_CINEMA = LocalDate.of(1895, 12, 28);

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Collection<Film> all() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        return filmStorage.getById(id).
                orElseThrow(() -> new NotFoundException("Не найден фильм с id: " + id));
    }

    public Film create(Film newFilm) {
        log.info("create, input object {}", newFilm);

        validate(newFilm);

        Film createdFilm = filmStorage.create(newFilm);

        log.info("create, output object {}", createdFilm);
        return createdFilm;
    }

    public Film update(Film filmToUpdate) {
        log.info("update, input object {}", filmToUpdate);

        int id = filmToUpdate.getId();
        if (filmStorage.getById(id).isEmpty()) {
            throw new NotFoundException(String.format("Не найден элемент с id=%d", id));
        }

        validate(filmToUpdate);

        Film updatedFilm = filmStorage.update(filmToUpdate);

        log.info("output object: {}", updatedFilm);
        return updatedFilm;
    }

    private void validate(Film film) {
        LocalDate releaseDate = film.getReleaseDate();
        if (releaseDate != null && releaseDate.isBefore(BIRTHDAY_OF_CINEMA)) {
            throw new ValidationException(String.format("Дата релиза должна быть не раньше %s", BIRTHDAY_OF_CINEMA));
        }
    }
}
