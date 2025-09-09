package event.eventmanagertask.event;

import event.eventmanagertask.model.Event;

public record EventKafkaEvent(
        Long eventId,
        EventType eventType,
        Event event
) {
}
