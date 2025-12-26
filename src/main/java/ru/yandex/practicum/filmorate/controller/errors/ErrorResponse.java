package ru.yandex.practicum.filmorate.controller.errors;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class ErrorResponse {
    private String error;
}
