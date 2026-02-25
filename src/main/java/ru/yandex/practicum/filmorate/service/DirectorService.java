package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbStorage directorDbStorage;

    public Director getById(int id) {
        return directorDbStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Не найден директор с id: " + id));
    }

    public List<Director> getAll() {
        return directorDbStorage.getAll();
    }

    public Director create(Director newDirector) {
        log.info("create, input object {}", newDirector);

        Director createdDirector = directorDbStorage.create(newDirector);

        log.info("create, output object {}", createdDirector);
        return createdDirector;
    }

    public Director update(Director directorToUpdate) {
        log.info("update, input object {}", directorToUpdate);

        checkDirectorExists(directorToUpdate.getId());

        Director updatedDirector = directorDbStorage.update(directorToUpdate);

        log.info("output object: {}", updatedDirector);
        return updatedDirector;
    }

    public void deleteById(int id) {
        checkDirectorExists(id);
        directorDbStorage.delete(id);
    }

    public void checkDirectorExists(int id) {
        directorDbStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Не найден директор с id:" + id));
    }

    public void checkDirectorsExists(List<Integer> ids) {

        List<Integer> receivedIds = directorDbStorage.getDirectorsByIds(ids)
                .stream()
                .map(Director::getId)
                .toList();

        List<String> notFoundIds = ids.stream()
                .filter(id -> !receivedIds.contains(id))
                .map(String::valueOf)
                .toList();

        if (!notFoundIds.isEmpty()) {
            String errorText = String.format("Не найдены режиссеры с идентификаторами: %s", notFoundIds);
            throw new NotFoundException(errorText);
        }
    }
}
