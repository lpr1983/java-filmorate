package ru.yandex.practicum.filmorate.aspect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.annotation.SaveUserAction;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.service.UserFeedService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Andrew Vilkov
 * @created 21.02.2026 - 20:14
 * @project java-filmorate
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:dummy;DB_CLOSE_DELAY=-1"
})
class UserActionAspectTest {
    @Mock
    private UserFeedService userFeedService;

    @InjectMocks
    private UserActionAspect userActionAspect;

    @Test
    @DisplayName("Проверяем что вызывается метод create сервиса UserFeedService для filmFeedAction")
    void testFilmFeedActionCallsCreate() {
        int userId = 42;
        int filmId = 100;
        SaveUserAction action = mock(SaveUserAction.class);
        when(action.event()).thenReturn(EventType.LIKE);
        when(action.operation()).thenReturn(OperationType.ADD);


        userActionAspect.filmFeedAction(filmId, userId, action);
        verify(userFeedService, times(1)).create(any());
    }

    @Test
    @DisplayName("Проверяем что вызывается метод create сервиса UserFeedService для friendFeedAction")
    void testFriendFeedActionCallsCreate() {
        int userId = 42;
        int filmId = 100;
        SaveUserAction action = mock(SaveUserAction.class);
        when(action.event()).thenReturn(EventType.LIKE);
        when(action.operation()).thenReturn(OperationType.ADD);


        userActionAspect.friendFeedAction(filmId, userId, action);
        verify(userFeedService, times(1)).create(any());
    }

}