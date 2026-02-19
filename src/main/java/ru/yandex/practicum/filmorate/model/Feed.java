package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@Builder
@EqualsAndHashCode
public class Feed {
    private Integer eventId;
    private Integer entityId;
    private EventType eventType;
    private OperationType operation;
    private Integer userId;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER,
            without = JsonFormat.Feature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
    private Instant timestamp;
}
