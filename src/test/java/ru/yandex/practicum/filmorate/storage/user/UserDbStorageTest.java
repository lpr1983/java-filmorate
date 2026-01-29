// src/test/java/ru/yandex/practicum/filmorate/storage/user/UserDbStorageIT.java
package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void create_and_getById() {
        User u = new User();
        u.setEmail("a@a.ru");
        u.setLogin("loginA");
        u.setName("A");
        u.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userStorage.create(u);

        assertThat(created.getId()).isNotNull();
        assertThat(userStorage.getById(created.getId()))
                .isPresent()
                .hasValueSatisfying(found -> {
                    assertThat(found.getEmail()).isEqualTo("a@a.ru");
                    assertThat(found.getLogin()).isEqualTo("loginA");
                    assertThat(found.getName()).isEqualTo("A");
                });
    }

    @Test
    void update() {
        User u = new User();
        u.setEmail("b@b.ru");
        u.setLogin("loginB");
        u.setName("B");
        u.setBirthday(LocalDate.of(1999, 2, 2));
        u = userStorage.create(u);

        u.setName("B2");
        User updated = userStorage.update(u);

        assertThat(updated.getName()).isEqualTo("B2");
        assertThat(userStorage.getById(u.getId()))
                .isPresent()
                .hasValueSatisfying(found -> assertThat(found.getName()).isEqualTo("B2"));
    }

    @Test
    void delete() {
        User u = new User();
        u.setEmail("c@c.ru");
        u.setLogin("loginC");
        u.setName("C");
        u.setBirthday(LocalDate.of(1990, 3, 3));
        u = userStorage.create(u);

        userStorage.delete(u.getId());

        assertThat(userStorage.getById(u.getId())).isEmpty();
    }

    @Test
    void friends_add_delete_get_common() {
        User u1 = new User();
        u1.setEmail("u1@t.ru");
        u1.setLogin("u1");
        u1.setName("u1");
        u1.setBirthday(LocalDate.of(1991, 1, 1));
        u1 = userStorage.create(u1);

        User u2 = new User();
        u2.setEmail("u2@t.ru");
        u2.setLogin("u2");
        u2.setName("u2");
        u2.setBirthday(LocalDate.of(1992, 2, 2));
        u2 = userStorage.create(u2);

        User u3 = new User();
        u3.setEmail("u3@t.ru");
        u3.setLogin("u3");
        u3.setName("u3");
        u3.setBirthday(LocalDate.of(1993, 3, 3));
        u3 = userStorage.create(u3);

        userStorage.addFriend(u1.getId(), u3.getId());
        userStorage.addFriend(u2.getId(), u3.getId());

        assertThat(userStorage.getFriends(u1.getId()))
                .extracting(User::getId)
                .containsExactly(u3.getId());

        assertThat(userStorage.getCommonFriends(u1.getId(), u2.getId()))
                .extracting(User::getId)
                .containsExactly(u3.getId());

        userStorage.deleteFriend(u1.getId(), u3.getId());
        assertThat(userStorage.getFriends(u1.getId())).isEmpty();
    }
}
