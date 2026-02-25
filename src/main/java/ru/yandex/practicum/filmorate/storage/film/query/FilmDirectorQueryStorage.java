package ru.yandex.practicum.filmorate.storage.film.query;

import ru.yandex.practicum.filmorate.model.DirectorSortBy;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmDirectorQueryStorage {
    List<Film> getFilmsByDirector(int directorId, DirectorSortBy sortBy);
}
