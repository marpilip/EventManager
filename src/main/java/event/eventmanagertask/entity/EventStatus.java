package event.eventmanagertask.entity;

import java.util.Arrays;

public enum EventStatus {
    WAIT_START(0),
    STARTED(1),
    CANCELLED(2),
    FINISHED(3);

    private final int code;

    EventStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static EventStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid status code: " + code));
    }
}
