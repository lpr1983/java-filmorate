package ru.yandex.practicum.filmorate.model;

import java.util.Optional;

public enum DirectorSortBy {
    YEAR,
    LIKES;

    public static Optional<DirectorSortBy> from(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return switch (value.toLowerCase()) {
            case "year" -> Optional.of(YEAR);
            case "likes" -> Optional.of(LIKES);
            default -> Optional.empty();
        };
    }
}