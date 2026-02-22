package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

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
    public List<Film> getPopular(int count, Integer genreId, Integer year) {
        return films.values().stream()
                // фильтр по жанру
                .filter(f -> genreId == null || f.getGenres().stream().anyMatch(g -> g.getId() == genreId))
                // фильтр по году
                .filter(f -> year == null || f.getReleaseDate().getYear() == year)
                // сортировка по количеству лайков (убывание)
                .sorted((f1, f2) -> Integer.compare(
                        likesByUsers.getOrDefault(f2.getId(), Collections.emptySet()).size(),
                        likesByUsers.getOrDefault(f1.getId(), Collections.emptySet()).size()
                ))
                // лимит count
                .limit(count)
                .toList();
    }

    private int getNextId() {
        nextId++;
        return nextId;
    }

}
