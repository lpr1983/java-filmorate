package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage mpaDbStorage;

    public List<MpaRating> getAll() {
        return mpaDbStorage.getAll();
    }

    public MpaRating getById(int id) {
        return mpaDbStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Не найден mpa с id=" + id));
    }

    public void checkMpaExists(int id) {
        mpaDbStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Не найден mpa с id=" + id));
    }

}
