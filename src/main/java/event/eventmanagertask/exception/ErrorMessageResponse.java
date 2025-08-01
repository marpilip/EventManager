package event.eventmanagertask.exception;

import java.time.LocalDateTime;

public record ErrorMessageResponse(String message, LocalDateTime timestamp) {
}
