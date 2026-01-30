package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage genreDbStorage;

    public Collection<Genre> getAll() {
        return genreDbStorage.getAll();
    }

    public Genre getById(int id) {
        return genreDbStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Не найден жанр с id: " + id));
    }

    public void checkGenreExists(int id) {
        genreDbStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Не найден жанр с id: " + id));
    }

    public void checkGenresExists(List<Integer> ids) {

        // Получаю список жанров по идентификаторам ids, извлекаю из них идентификаторы
        List<Integer> receivedIds = genreDbStorage.getGenresByIds(ids)
                .stream()
                .map(Genre::getId)
                .toList();

        // Проверяю, все ли жанры пришли из БД
        List<String> notFoundIds = ids.stream()
                .filter(id -> !receivedIds.contains(id))
                .map(String::valueOf)
                .toList();

        if (!notFoundIds.isEmpty()) {
            String errorText = String.format("Не найдены жанры с идентификаторами: %s", notFoundIds);
            throw new NotFoundException(errorText);
        }
    }

}
