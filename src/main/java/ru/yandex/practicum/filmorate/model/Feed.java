package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Feed {
    Long eventId;
    Long userId;
    Long timestamp;
    FeedEventType eventType;
    FeedOperationType operation;
    Long entityId;
}
