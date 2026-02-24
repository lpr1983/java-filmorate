package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSearchBy;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.model.DirectorSortBy;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.query.FilmSearchQueryDbStorage;
import ru.yandex.practicum.filmorate.storage.film.query.FilmDirectorQueryDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final GenreDbStorage genreDbStorage;
    private final UserService userService;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final FilmSearchQueryDbStorage filmSearchQueryDbStorage;
    private final DirectorService directorService;
    private final DirectorDbStorage directorDbStorage;
    private final FilmDirectorQueryDbStorage filmDirectorQueryDbStorage;
    public static final LocalDate BIRTHDAY_OF_CINEMA = LocalDate.of(1895, 12, 28);

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserService userService,
                       MpaService mpaService,
                       GenreService genreService,
                       GenreDbStorage genreDbStorage,
                       DirectorService directorService,
                       DirectorDbStorage directorDbStorage,
                       FilmDirectorQueryDbStorage filmDirectorQueryDbStorage,
                       FilmSearchQueryDbStorage filmSearchQueryDbStorage
    ) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.genreDbStorage = genreDbStorage;
        this.directorService = directorService;
        this.directorDbStorage = directorDbStorage;
        this.filmDirectorQueryDbStorage = filmDirectorQueryDbStorage;
        this.filmSearchQueryDbStorage = filmSearchQueryDbStorage;
    }

    public List<Film> all() {

        List<Film> result = filmStorage.getAll();

        genreDbStorage.joinGenresToFilms(result);
        directorDbStorage.joinDirectorsToFilms(result);

        return result;
    }

    public Film getById(int id) {

        Film result = filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Не найден фильм с id: " + id));

        genreDbStorage.joinGenresToFilms(List.of(result));
        directorDbStorage.joinDirectorsToFilms(List.of(result));

        return result;
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

        if (newFilm.getDirectors() != null) {
            directorService.checkDirectorsExists(newFilm.getDirectors().stream()
                    .map(Director::getId)
                    .distinct()
                    .toList());
        }

        Film createdFilm = filmStorage.create(newFilm);

        List<Film> filmToUpdateGenresAndDirectors = List.of(createdFilm);
        genreDbStorage.joinGenresToFilms(filmToUpdateGenresAndDirectors);
        directorDbStorage.joinDirectorsToFilms(filmToUpdateGenresAndDirectors);

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

        if (filmToUpdate.getDirectors() != null) {
            directorService.checkDirectorsExists(filmToUpdate.getDirectors().stream()
                    .map(Director::getId)
                    .distinct()
                    .toList());
        }

        Film updatedFilm = filmStorage.update(filmToUpdate);

        List<Film> filmToUpdateGenresAndDirectors = List.of(updatedFilm);
        genreDbStorage.joinGenresToFilms(filmToUpdateGenresAndDirectors);
        directorDbStorage.joinDirectorsToFilms(filmToUpdateGenresAndDirectors);

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
        return getPopular(count, null, null);
    }

    public List<Film> getPopular(int count, Integer genreId, Integer year) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть больше 0");
        }

        // Получаем список популярных фильмов с фильтрацией по жанру и году
        List<Film> popular = filmStorage.getPopular(count, genreId, year);

        // Для DB-хранилища подгружаем жанры и директоров
        genreDbStorage.joinGenresToFilms(popular);
        directorDbStorage.joinDirectorsToFilms(popular);

        log.debug("getPopular, count = {}, genreId = {}, year = {}, resultSize = {}",
                count, genreId, year, popular.size());

        return popular;
    }

    public List<Film> getFilmsByDirector(int directorId, DirectorSortBy sortBy) {
        directorService.checkDirectorExists(directorId);

        log.debug("getFilmsOfDirector, directorId = {}, sortBy = {}", directorId, sortBy);

        List<Film> filmsOfDirector = filmDirectorQueryDbStorage.getFilmsByDirector(directorId, sortBy);

        genreDbStorage.joinGenresToFilms(filmsOfDirector);
        directorDbStorage.joinDirectorsToFilms(filmsOfDirector);

        return filmsOfDirector;
    }

    public List<Film> search(String query, String by) {
        log.info("search, query = {}, by = {}", query, by);

        if (query.isBlank()) {
            throw new ValidationException("Query не должен быть пустой");
        }
        if (by.isBlank()) {
            throw new ValidationException("By не должен быть пустой");
        }

        Set<FilmSearchBy> searchCases = new HashSet<>();

        for (String s : by.toLowerCase().split(",")) {
            String trimmed = s.trim();

            FilmSearchBy searchBy = switch (trimmed) {
                case "title" -> FilmSearchBy.TITLE;
                case "director" -> FilmSearchBy.DIRECTOR;
                default -> throw new ValidationException("Неизвестный вариант by: " + trimmed);
            };

            searchCases.add(searchBy);
        }

        List<Film> foundFilms = filmSearchQueryDbStorage.search(query, searchCases);

        genreDbStorage.joinGenresToFilms(foundFilms);
        directorDbStorage.joinDirectorsToFilms(foundFilms);

        return foundFilms;
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

    // Получение фильмов, которые лайкнули оба пользователя
    public List<Film> getCommonFilms(int userId, int friendId) {

        userService.checkUserExists(userId);
        userService.checkUserExists(friendId);

        List<Film> foundFilms = filmStorage.getCommonFilms(userId, friendId);

        // Обогащаем жанрами
        genreDbStorage.joinGenresToFilms(foundFilms);
        // Обогащаем режиссёрами
        directorDbStorage.joinDirectorsToFilms(foundFilms);

        return foundFilms;
    }

}
