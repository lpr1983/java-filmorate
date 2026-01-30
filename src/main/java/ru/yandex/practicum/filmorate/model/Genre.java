package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Genre {
    @NotNull
    private final int id;
    private final String name;
}
