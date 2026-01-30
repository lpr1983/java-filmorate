package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreOfFilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmDbStorage.class,
        FilmRowMapper.class,
        GenreDbStorage.class,
        GenreOfFilmRowMapper.class,
        GenreRowMapper.class,
        UserDbStorage.class,
        UserRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final GenreDbStorage genreDbStorage;

    @Test
    void create_and_getById_with_genres() {
        Film f = new Film();
        f.setName("Film");
        f.setDescription("Desc");
        f.setReleaseDate(LocalDate.of(2020, 1, 1));
        f.setDuration(100);
        f.setGenres(Set.of(new Genre(1, "Комедия"), new Genre(2, "Драма")));

        Film created = filmStorage.create(f);

        assertThat(created.getId()).isNotNull();
        assertThat(filmStorage.getById(created.getId()))
                .isPresent()
                .hasValueSatisfying(found -> {
                    genreDbStorage.joinGenresToFilms(List.of(found));
                    assertThat(found.getName()).isEqualTo("Film");
                    assertThat(found.getGenres()).extracting(Genre::getId).contains(1, 2);
                });
    }

    @Test
    void updateAndRewriteGenres() {
        Film f = new Film();
        f.setName("F1");
        f.setDescription("D1");
        f.setReleaseDate(LocalDate.of(2020, 1, 1));
        f.setDuration(90);
        f.setGenres(Set.of(new Genre(1, "Комедия")));
        f = filmStorage.create(f);

        f.setGenres(Set.of(new Genre(2, "Драма")));
        filmStorage.update(f);

        assertThat(filmStorage.getById(f.getId()))
                .isPresent()
                .hasValueSatisfying(found -> {
                            genreDbStorage.joinGenresToFilms(List.of(found));
                            assertThat(found.getGenres()).extracting(Genre::getId).containsExactly(2);
                        }
                );
    }

    @Test
    void likes_and_popular_order() {
        User u1 = new User();
        u1.setEmail("l1@t.ru");
        u1.setLogin("l1");
        u1.setName("l1");
        u1.setBirthday(LocalDate.of(1990, 1, 1));
        u1 = userStorage.create(u1);

        User u2 = new User();
        u2.setEmail("l2@t.ru");
        u2.setLogin("l2");
        u2.setName("l2");
        u2.setBirthday(LocalDate.of(1991, 1, 1));
        u2 = userStorage.create(u2);

        Film f1 = new Film();
        f1.setName("P1");
        f1.setDescription("d");
        f1.setReleaseDate(LocalDate.of(2020, 1, 1));
        f1.setDuration(100);
        f1 = filmStorage.create(f1);

        Film f2 = new Film();
        f2.setName("P2");
        f2.setDescription("d");
        f2.setReleaseDate(LocalDate.of(2020, 1, 1));
        f2.setDuration(100);
        f2 = filmStorage.create(f2);

        filmStorage.addLike(f1.getId(), u1.getId());
        filmStorage.addLike(f1.getId(), u2.getId());
        filmStorage.addLike(f2.getId(), u1.getId());

        assertThat(filmStorage.getPopular(2))
                .extracting(Film::getId)
                .containsExactly(f1.getId(), f2.getId());

        filmStorage.deleteLike(f1.getId(), u2.getId());
        filmStorage.deleteLike(f1.getId(), u1.getId());

        assertThat(filmStorage.getPopular(2))
                .extracting(Film::getId)
                .containsExactly(f2.getId(), f1.getId());
    }

    @Test
    void delete_removes_film() {
        Film f = new Film();
        f.setName("Del");
        f.setDescription("d");
        f.setReleaseDate(LocalDate.of(2020, 1, 1));
        f.setDuration(100);
        f = filmStorage.create(f);

        filmStorage.delete(f.getId());

        assertThat(filmStorage.getById(f.getId())).isEmpty();
    }

    @Test
    void create_with_null_mpa_does_not_throw_and_persists() {
        Film f = new Film();
        f.setName("NoMPA");
        f.setDescription("d");
        f.setReleaseDate(LocalDate.of(2020, 1, 1));
        f.setDuration(100);
        f.setMpa(null);

        Film created = filmStorage.create(f);

        assertThat(created.getId()).isNotNull();
        assertThat(filmStorage.getById(created.getId()))
                .isPresent()
                .hasValueSatisfying(found -> assertThat(found.getMpa()).isNull());
    }
}
