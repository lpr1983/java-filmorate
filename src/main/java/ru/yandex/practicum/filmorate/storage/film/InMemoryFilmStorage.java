package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private final Map<Integer, Set<Integer>> likesByUsers = new HashMap<>();
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

    @Override
    public void addLike(int filmId, int userId) {
        likesByUsers.computeIfAbsent(filmId, key -> new HashSet<>()).add(userId);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        Set<Integer> likes = likesByUsers.get(filmId);
        likes.remove(userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        return likesByUsers.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .map(Map.Entry::getKey)
                .limit(count)
                .map(films::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private int getNextId() {
        nextId++;
        return nextId;
    }

}
