package event.eventmanagertask.exception;

import java.time.LocalDateTime;

public record ServerErrorDto(
        String message,
        String detailMessage,
        LocalDateTime timestamp
) {
}
