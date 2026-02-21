package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Review {
    private Integer reviewId;

    @NotBlank(message = "Отзыв не может быть пустым")
    @Size(max = 1000, message = "Максимальная длина отзыва — 1000 символов")
    private String content;

    @NotNull
    private Boolean isPositive;

    @NotNull
    private Integer filmId;

    @NotNull
    private Integer userId;

    private Integer useful;
}
