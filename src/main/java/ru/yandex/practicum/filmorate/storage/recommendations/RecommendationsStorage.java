package ru.yandex.practicum.filmorate.storage.recommendations;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface RecommendationsStorage {
    List<Film> getRecommendations(int userId);
}