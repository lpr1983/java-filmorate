package ru.yandex.practicum.filmorate.storage.film.query;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSearchBy;

import java.util.List;
import java.util.Set;

public interface FilmSearchQueryStorage {
    List<Film> search(String query, Set<FilmSearchBy> by);
}