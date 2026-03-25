package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.model.FeedEventType;
import ru.yandex.practicum.filmorate.model.FeedOperationType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long eventId;
    Long userId;
    Long timestamp;
    FeedEventType eventType;
    FeedOperationType operation;
    Long entityId;
}
