package ru.yandex.practicum.filmorate.storage.film;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.model.Genre;

@Data
@RequiredArgsConstructor
public class GenreOfFilm {
    private final int filmId;
    private final Genre genre;
}
