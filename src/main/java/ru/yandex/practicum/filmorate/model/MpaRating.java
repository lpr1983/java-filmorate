package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MpaRating {
    @NotNull
    private final int id;
    private final String name;
    private final Integer age;
}
