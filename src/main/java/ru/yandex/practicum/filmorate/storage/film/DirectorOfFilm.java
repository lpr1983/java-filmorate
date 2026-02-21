package ru.yandex.practicum.filmorate.storage.film;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.model.Director;

@Data
@RequiredArgsConstructor
public class DirectorOfFilm {
    private final int filmId;
    private final Director director;
}
