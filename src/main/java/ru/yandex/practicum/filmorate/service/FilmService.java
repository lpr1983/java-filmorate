package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final MpaService mpaService;
    private final GenreService genreService;
    public static final LocalDate BIRTHDAY_OF_CINEMA = LocalDate.of(1895, 12, 28);

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserService userService,
                       MpaService mpaService,
                       GenreService genreService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public List<Film> all() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {

        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Не найден фильм с id: " + id));
    }

    public Film create(Film newFilm) {
        log.info("create, input object {}", newFilm);

        validate(newFilm);

        if (newFilm.getMpa() != null) {
            mpaService.checkMpaExists(newFilm.getMpa().getId());
        }

        if (newFilm.getGenres() != null) {
            genreService.checkGenresExists(newFilm.getGenres().stream()
                    .map(Genre::getId)
                    .distinct()
                    .toList());
        }

        Film createdFilm = filmStorage.create(newFilm);

        log.info("create, output object {}", createdFilm);
        return createdFilm;
    }

    public Film update(Film filmToUpdate) {
        log.info("update, input object {}", filmToUpdate);

        validate(filmToUpdate);

        checkFilmExists(filmToUpdate.getId());

        if (filmToUpdate.getMpa() != null) {
            mpaService.checkMpaExists(filmToUpdate.getMpa().getId());
        }

        if (filmToUpdate.getGenres() != null) {
            genreService.checkGenresExists(filmToUpdate.getGenres().stream()
                    .map(Genre::getId)
                    .distinct()
                    .toList());
        }

        Film updatedFilm = filmStorage.update(filmToUpdate);

        log.info("output object: {}", updatedFilm);
        return updatedFilm;
    }

    public void deleteById(int id) {
        checkFilmExists(id);
        filmStorage.delete(id);
    }

    public void addLike(int id, int userId) {
        userService.checkUserExists(userId);
        checkFilmExists(id);

        filmStorage.addLike(id, userId);

        log.info("Like added: Id={}, userId={}", id, userId);
    }

    public void deleteLike(int id, int userId) {
        userService.checkUserExists(userId);
        checkFilmExists(id);

        filmStorage.deleteLike(id, userId);

        log.info("Like deleted: Id={}, userId={}", id, userId);
    }

    public List<Film> getPopular(int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть больше 0");
        }

        List<Film> popular = filmStorage.getPopular(count);

        log.debug("getPopular, count = {}, resultSize = {}", count, popular.size());
        return popular;
    }

    public void checkFilmExists(int id) {
        filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Не найден фильм с id:" + id));
    }

    private void validate(Film film) {
        LocalDate releaseDate = film.getReleaseDate();
        if (releaseDate != null && releaseDate.isBefore(BIRTHDAY_OF_CINEMA)) {
            throw new ValidationException(String.format("Дата релиза должна быть не раньше %s", BIRTHDAY_OF_CINEMA));
        }
    }

}
