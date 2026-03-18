package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FeedDto;
import ru.yandex.practicum.filmorate.mapper.FeedMapper;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.FeedRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class FeedService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    public List<FeedDto> getUserFeed(Long userId) {
        User u = userRepository.getUser(userId);
        Collection<Feed> feeds = feedRepository.getEventsByUserId(userId);
        return feeds.stream()
                .map(FeedMapper::mapToFeedDto)
                .collect(Collectors.toList());
    }
}
