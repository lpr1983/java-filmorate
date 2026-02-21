package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.DbStorageException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mapper.*;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        ReviewDbStorage.class,
        ReviewRowMapper.class,
        FilmDbStorage.class,
        FilmRowMapper.class,
        UserDbStorage.class,
        UserRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ReviewDbStorageTest {
    private final ReviewDbStorage reviewStorage;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film testFilm;
    private User testUser1;
    private User testUser2;
    private User testUser3;
    private Review testReview;

    @BeforeEach
    void beforeEach() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        film.setGenres(Set.of(new Genre(1, "Комедия"), new Genre(2, "Драма")));
        testFilm = filmStorage.create(film);

        User user = new User();
        user.setEmail("a1@a.ru");
        user.setLogin("loginA1");
        user.setName("A1");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        testUser1 = userStorage.create(user);

        user = new User();
        user.setEmail("a2@a.ru");
        user.setLogin("loginA2");
        user.setName("A2");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        testUser2 = userStorage.create(user);

        user = new User();
        user.setEmail("a3@a.ru");
        user.setLogin("loginA3");
        user.setName("A3");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        testUser3 = userStorage.create(user);

        testReview = new Review();
        testReview.setContent("content");
        testReview.setIsPositive(true);
        testReview.setFilmId(testFilm.getId());
        testReview.setUserId(testUser1.getId());
    }

    @Test
    void createReviewTest() {
        Review createdReview = reviewStorage.create(testReview);

        assertAll(
                () -> assertNotEquals(0, createdReview.getReviewId()),
                () -> assertEquals(testReview.getContent(), createdReview.getContent()),
                () -> assertEquals(testReview.getIsPositive(), createdReview.getIsPositive()),
                () -> assertEquals(testReview.getUserId(), createdReview.getUserId()),
                () -> assertEquals(testReview.getFilmId(), createdReview.getFilmId()),
                () -> assertEquals(0, createdReview.getUseful())
        );
    }

    @Test
    void getReviewByIdTest() {
        Integer id = reviewStorage.create(testReview).getReviewId();

        Optional<Review> optionalReview = reviewStorage.getById(id);
        assertTrue(optionalReview.isPresent());
        Review reviewById = optionalReview.get();
        assertAll(
                () -> assertEquals(id, reviewById.getReviewId()),
                () -> assertEquals(testReview.getContent(), reviewById.getContent()),
                () -> assertEquals(testReview.getIsPositive(), reviewById.getIsPositive()),
                () -> assertEquals(testReview.getUserId(), reviewById.getUserId()),
                () -> assertEquals(testReview.getFilmId(), reviewById.getFilmId()),
                () -> assertEquals(0, reviewById.getUseful())
        );
    }


    @Test
    void updateReviewTest() {
        Review createdReview = reviewStorage.create(testReview);

        createdReview.setContent("newContent");
        createdReview.setIsPositive(false);
        createdReview.setUseful(10);
        reviewStorage.update(createdReview);

        Optional<Review> optionalReview = reviewStorage.getById(createdReview.getReviewId());
        assertTrue(optionalReview.isPresent());
        Review updatedReview = optionalReview.get();
        assertAll(
                () -> assertEquals(createdReview.getContent(), updatedReview.getContent()),
                () -> assertEquals(createdReview.getIsPositive(), updatedReview.getIsPositive()),
                () -> assertEquals(createdReview.getUserId(), updatedReview.getUserId()),
                () -> assertEquals(createdReview.getFilmId(), updatedReview.getFilmId()),
                () -> assertEquals(0, updatedReview.getUseful())
        );
    }

    @Test
    void delete() {
        Review createdReview = reviewStorage.create(testReview);

        reviewStorage.delete(createdReview.getReviewId());
        Optional<Review> optionalReview = reviewStorage.getById(createdReview.getReviewId());

        assertTrue(optionalReview.isEmpty());
    }


    @Test
    void addLikeToReviewTest() {
        Integer id = reviewStorage.create(testReview).getReviewId();
        Review review = reviewStorage.getById(id)
                .orElseThrow(() -> new DbStorageException("Ошибка БД во время тестов"));

        assertEquals(0, review.getUseful());

        reviewStorage.addLike(id, testUser1.getId());
        review = reviewStorage.getById(id)
                .orElseThrow(() -> new DbStorageException("Ошибка БД во время тестов"));
        assertEquals(1, review.getUseful());

        reviewStorage.addLike(id, testUser2.getId());
        reviewStorage.addLike(id, testUser3.getId());
        review = reviewStorage.getById(id)
                .orElseThrow(() -> new DbStorageException("Ошибка БД во время тестов"));
        assertEquals(3, review.getUseful());

        reviewStorage.addLike(id, testUser1.getId());
        reviewStorage.addLike(id, testUser2.getId());
        reviewStorage.addLike(id, testUser3.getId());
        review = reviewStorage.getById(id)
                .orElseThrow(() -> new DbStorageException("Ошибка БД во время тестов"));
        assertEquals(3, review.getUseful());
    }

    @Test
    void addDislikeToReviewTest() {
        Integer id = reviewStorage.create(testReview).getReviewId();
        Review review = reviewStorage.getById(id)
                .orElseThrow(() -> new DbStorageException("Ошибка БД во время тестов"));

        assertEquals(0, review.getUseful());

        reviewStorage.addDislike(id, testUser1.getId());
        review = reviewStorage.getById(id)
                .orElseThrow(() -> new DbStorageException("Ошибка БД во время тестов"));
        assertEquals(-1, review.getUseful());

        reviewStorage.addDislike(id, testUser2.getId());
        reviewStorage.addDislike(id, testUser3.getId());
        review = reviewStorage.getById(id)
                .orElseThrow(() -> new DbStorageException("Ошибка БД во время тестов"));
        assertEquals(-3, review.getUseful());

        reviewStorage.addDislike(id, testUser1.getId());
        reviewStorage.addDislike(id, testUser2.getId());
        reviewStorage.addDislike(id, testUser3.getId());
        review = reviewStorage.getById(id)
                .orElseThrow(() -> new DbStorageException("Ошибка БД во время тестов"));
        assertEquals(-3, review.getUseful());

        reviewStorage.addLike(id, testUser3.getId());
        review = reviewStorage.getById(id)
                .orElseThrow(() -> new DbStorageException("Ошибка БД во время тестов"));
        assertEquals(-1, review.getUseful());
    }

    @Test
    void deleteLike() {
        Integer id = reviewStorage.create(testReview).getReviewId();
        reviewStorage.addLike(id, testUser1.getId());
        reviewStorage.deleteLike(id, testUser1.getId());
        Review review = reviewStorage.getById(id)
                .orElseThrow(() -> new DbStorageException("Ошибка БД во время тестов"));

        assertEquals(0, review.getUseful());

        reviewStorage.addDislike(id, testUser2.getId());
        reviewStorage.deleteLike(id, testUser2.getId());
        review = reviewStorage.getById(id)
                .orElseThrow(() -> new DbStorageException("Ошибка БД во время тестов"));

        assertEquals(0, review.getUseful());
    }

    @Test
    void getAllReviews() {
        Integer id1 = reviewStorage.create(testReview).getReviewId();
        Integer id2 = reviewStorage.create(testReview).getReviewId();
        Integer id3 = reviewStorage.create(testReview).getReviewId();
        Integer id4 = reviewStorage.create(testReview).getReviewId();

        reviewStorage.addLike(id2, testUser1.getId());
        reviewStorage.addLike(id2, testUser2.getId());
        reviewStorage.addLike(id2, testUser3.getId());
        reviewStorage.addLike(id3, testUser1.getId());
        reviewStorage.addLike(id3, testUser2.getId());
        reviewStorage.addDislike(id4, testUser1.getId());

        List<Review> reviews = reviewStorage.getAllReviews(4);
        assertIterableEquals(
                List.of(id2, id3, id1, id4),
                reviews.stream().map(Review::getReviewId).toList()
        );
    }

    @Test
    void getReviewsByFilmId() {
        Integer filmId1 = testFilm.getId();
        Integer filmId2 = filmStorage.create(testFilm).getId();

        testReview.setFilmId(filmId1);
        Integer id1 = reviewStorage.create(testReview).getReviewId();
        Integer id2 = reviewStorage.create(testReview).getReviewId();
        Integer id3 = reviewStorage.create(testReview).getReviewId();

        testReview.setFilmId(filmId2);
        Integer id4 = reviewStorage.create(testReview).getReviewId();
        Integer id5 = reviewStorage.create(testReview).getReviewId();

        reviewStorage.addLike(id2, testUser1.getId());
        reviewStorage.addLike(id2, testUser2.getId());
        reviewStorage.addLike(id2, testUser3.getId());
        reviewStorage.addLike(id3, testUser1.getId());
        reviewStorage.addLike(id3, testUser2.getId());
        reviewStorage.addLike(id5, testUser3.getId());
        reviewStorage.addDislike(id4, testUser2.getId());

        List<Review> reviews = reviewStorage.getReviewsByFilmId(filmId1, 3);
        assertIterableEquals(
                List.of(id2, id3, id1),
                reviews.stream().map(Review::getReviewId).toList()
        );
    }
}