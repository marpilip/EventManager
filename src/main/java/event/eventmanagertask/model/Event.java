package event.eventmanagertask.model;

import event.eventmanagertask.entity.EventStatus;

import java.time.LocalDateTime;
import java.util.List;

public record Event(
        Long id,
        String name,
        Long ownerId,
        int maxPlaces,
        List<Registration> registrationList,
        LocalDateTime date,
        int cost,
        int duration,
        Long locationId,
        EventStatus status) {
}
