package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.storage.mapper.GenreOfFilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreDbStorage.class, GenreRowMapper.class, GenreOfFilmRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    void getAll_contains_seed_data() {
        assertThat(genreStorage.getAll()).isNotEmpty();
    }

    @Test
    void getById_found() {
        assertThat(genreStorage.getById(1))
                .isPresent()
                .hasValueSatisfying(g -> {
                    assertThat(g.getId()).isEqualTo(1);
                    assertThat(g.getName()).isNotBlank();
                });
    }

    @Test
    void getGenresByIds_returns_only_existing() {
        assertThat(genreStorage.getGenresByIds(List.of(1, 2, 999)))
                .extracting("id")
                .contains(1, 2)
                .doesNotContain(999);
    }
}
