package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.UserCreateDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.UserUpdateDto;
import ru.yandex.practicum.filmorate.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static User createDtoToUser(UserCreateDto dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setLogin(dto.getLogin());
        user.setName(dto.getName());
        user.setBirthday(dto.getBirthday());
        return user;
    }

    public static UserDto userToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setBirthday(user.getBirthday());
        return dto;
    }

    public static void updateUserFromDto(User user, UserUpdateDto dto) {
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getLogin() != null) user.setLogin(dto.getLogin());
        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getBirthday() != null) user.setBirthday(dto.getBirthday());
    }

}