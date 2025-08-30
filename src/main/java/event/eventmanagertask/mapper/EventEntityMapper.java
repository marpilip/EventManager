package event.eventmanagertask.mapper;

import event.eventmanagertask.entity.EventEntity;
import event.eventmanagertask.model.Event;
import event.eventmanagertask.model.Registration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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


    public List<Event> toDomainList(List<EventEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}
