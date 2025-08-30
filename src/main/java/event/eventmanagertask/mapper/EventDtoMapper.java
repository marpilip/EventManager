package event.eventmanagertask.mapper;

import event.eventmanagertask.dto.EventDto;
import event.eventmanagertask.model.Event;
import org.springframework.stereotype.Component;

@Component
public class EventDtoMapper {
    public EventDto toDto(Event event) {
        return new EventDto(
                event.id(),
                event.name(),
                event.ownerId(),
                event.maxPlaces(),
                event.registrationList().size(),
                event.date(),
                event.cost(),
                event.duration(),
                event.locationId(),
                event.status()
        );
    }
}
