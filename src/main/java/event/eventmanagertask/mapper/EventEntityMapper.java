package event.eventmanagertask.mapper;

import event.eventmanagertask.entity.EventEntity;
import event.eventmanagertask.model.Event;
import event.eventmanagertask.model.Registration;
import org.springframework.stereotype.Component;

@Component
public class EventEntityMapper {
    public Event toDomain(EventEntity entity) {
        return new Event(
                entity.getId(),
                entity.getName(),
                entity.getOwnerId(),
                entity.getMaxPlaces(),
                entity.getRegistrationList().stream()
                        .map(it -> new Registration(
                                it.getId(),
                                it.getUserId(),
                                entity.getId())
                        )
                        .toList(),
                entity.getDate(),
                entity.getCost(),
                entity.getDuration(),
                entity.getLocationId(),
                entity.getStatus()
        );
    }
}
