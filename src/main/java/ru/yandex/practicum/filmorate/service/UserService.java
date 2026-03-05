package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto postUser(NewUserRequest request) {
        log.debug("create user {}", request);
        User user = userRepository.save(UserMapper.mapToUser(request));
        return UserMapper.mapToUserDto(user);
    }

    @Transactional
    public UserDto putUser(UpdateUserRequest request) {
        log.debug("put user {}", request);
        User user = userRepository.getUser(request.getId());
        User newUser = UserMapper.updateUserFields(user, request);
        newUser = userRepository.update(newUser);
        return UserMapper.mapToUserDto(newUser);
    }

    public Collection<UserDto> getUsers() {
        log.debug("call getUsers");
        return userRepository.getUsers().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public UserDto getUser(Long id) {
        log.debug("call getUser by id {}", id);
        User user = userRepository.getUser(id);
        return UserMapper.mapToUserDto(user);
    }

    @Transactional
    public Collection<UserDto> addFriend(Long id, Long friendId) {
        log.debug("add friend {} user {}", friendId, id);
        Collection<User> users = userRepository.addFriend(id, friendId);
        return users.stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    @Transactional
    public Collection<UserDto> deleteFriend(Long id, Long friendId) {
        log.debug("delete friend {} user {}", friendId, id);
        Collection<User> users = userRepository.deleteFriend(id, friendId);
        return users.stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public Collection<UserDto> getFriends(Long id) {
        log.debug("call getFriends user {}", id);
        return userRepository.getFriends(id).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public Collection<UserDto> getCommonFriends(Long id, Long otherId) {
        log.debug("call getCommonFriends user {} other {}", id, otherId);
        return userRepository.getCommonFriends(id, otherId).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }
}
