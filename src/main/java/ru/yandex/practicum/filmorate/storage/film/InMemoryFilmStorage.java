package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 0;

    @Override
    public List<Film> getAll() {
        return films.values().stream().toList();
    }

    @Override
    public Optional<Film> getById(int filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public Film create(Film newFilm) {
        int newId = getNextId();
        newFilm.setId(newId);
        films.put(newId, newFilm);

        return newFilm;
    }

    @Override
    public Film update(Film filmToUpdate) {
        int id = filmToUpdate.getId();
        films.put(id, filmToUpdate);
        return filmToUpdate;
    }

    @Override
    public void delete(int filmId) {
        films.remove(filmId);
    }

    private int getNextId() {
        nextId++;
        return nextId;
    }
}
