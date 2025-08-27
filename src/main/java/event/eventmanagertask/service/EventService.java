package event.eventmanagertask.service;

import event.eventmanagertask.dto.EventCreateRequestDto;
import event.eventmanagertask.dto.EventUpdateRequestDto;
import event.eventmanagertask.dto.LocationDto;
import event.eventmanagertask.entity.EventEntity;
import event.eventmanagertask.entity.EventStatus;
import event.eventmanagertask.exception.ForbiddenException;
import event.eventmanagertask.mapper.EventEntityMapper;
import event.eventmanagertask.model.Event;
import event.eventmanagertask.model.Role;
import event.eventmanagertask.model.User;
import event.eventmanagertask.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {
    private static Logger logger = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final AuthenticationService authenticationService;
    private final EventEntityMapper eventEntityMapper;
    private final LocationService locationService;

    public EventService(EventRepository eventRepository, AuthenticationService authenticationService,
                        EventEntityMapper eventEntityMapper, LocationService locationService) {
        this.eventRepository = eventRepository;
        this.authenticationService = authenticationService;
        this.eventEntityMapper = eventEntityMapper;
        this.locationService = locationService;
    }

    public Event createEvent(EventCreateRequestDto createRequest) throws BadRequestException {
        LocationDto location = locationService.getLocationById(createRequest.getLocationId());

        if (location.getCapacity() < createRequest.getMaxPlaces()) {
            throw new BadRequestException("Максимальное количество мест превышает вместимость локации");
        }

        User currentUser = authenticationService.getCurrentAuthenticatedUserOrThrow();

        EventEntity eventEntity = new EventEntity(
                null,
                createRequest.getName(),
                createRequest.getLocationId(),
                createRequest.getMaxPlaces(),
                List.of(),
                createRequest.getDate(),
                createRequest.getCost(),
                createRequest.getDuration(),
                createRequest.getLocationId(),
                EventStatus.WAIT_START
        );

        eventEntity = eventRepository.save(eventEntity);

        return eventEntityMapper.toDomain(eventEntity);
    }

    public Event getEventById(Long eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Мероприятие не найдено"));

        return eventEntityMapper.toDomain(event);
    }

    public Event updateEvent(Long eventId, EventUpdateRequestDto updateRequest) throws BadRequestException {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Мероприятие не найдено"));

        User currentUser = authenticationService.getCurrentAuthenticatedUserOrThrow();

        if (currentUser.role() != Role.ADMIN && !event.getOwnerId().equals(currentUser.id())) {
            throw new ForbiddenException("Недостаточно прав для обновления мероприятия");
        }

        if (!event.getStatus().equals(EventStatus.WAIT_START)) {
            throw new BadRequestException("Нельзя изменять мероприятие в статусе: " + event.getStatus());
        }

        Event currentEvent = eventEntityMapper.toDomain(event);
        Event updatedEvent = applyUpdates(currentEvent, updateRequest);

        validateEventUpdate(updatedEvent, updateRequest);

        event = eventRepository.save(event);

        return eventEntityMapper.toDomain(event);
    }

    public void cancelEvent(Long eventId) throws BadRequestException {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Мероприятие не найдено"));

        Event eventToCancel = eventEntityMapper.toDomain(event);

        User currentUser = authenticationService.getCurrentAuthenticatedUserOrThrow();

        if (currentUser.role() != Role.ADMIN && !event.getOwnerId().equals(currentUser.id())) {
            throw new ForbiddenException("Недостаточно прав для обновления мероприятия");
        }

        if (!event.getStatus().equals(EventStatus.WAIT_START)) {
            if (event.getStatus().equals(EventStatus.CANCELLED)) {
                throw new BadRequestException("Мероприятие уже отменено");
            }

            if (event.getStatus().equals(EventStatus.FINISHED)) {
                throw new BadRequestException("Мероприятие уже закончено");
            }
        }

        if (eventToCancel.date().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Нельзя отменить мероприятие, которое уже началось");
        }

        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    public List<Event> getCurrentUserEvents() {
        var currentUser = authenticationService.getCurrentAuthenticatedUserOrThrow();
        var userEvents = eventRepository.findByUserIdRegistrations(currentUser.id());

        return userEvents.stream()
                .map(eventEntityMapper::toDomain)
                .toList();
    }

    private Event applyUpdates(Event currentEvent, EventUpdateRequestDto updateRequest) {
        return new Event(
                currentEvent.id(),
                updateRequest.getName() != null ? updateRequest.getName() : currentEvent.name(),
                currentEvent.ownerId(),
                updateRequest.getMaxPlaces() != null ? updateRequest.getMaxPlaces() : currentEvent.maxPlaces(),
                currentEvent.registrationList(),
                updateRequest.getDate() != null ? updateRequest.getDate() : currentEvent.date(),
                updateRequest.getCost() != null ? updateRequest.getCost() : currentEvent.cost(),
                updateRequest.getDuration() != null ? updateRequest.getDuration() : currentEvent.duration(),
                updateRequest.getLocationId() != null ? updateRequest.getLocationId() : currentEvent.locationId(),
                currentEvent.status()
        );
    }

    private void validateEventUpdate(Event updatedEvent, EventUpdateRequestDto updateRequest)
            throws BadRequestException {
        if (updateRequest.getMaxPlaces() != null) {
            LocationDto location = locationService.getLocationById(updatedEvent.locationId());

            if (location.getCapacity() < updateRequest.getMaxPlaces()) {
                throw new BadRequestException("Вместимость локации меньше максимального количества мест");
            }
        }

        if (updateRequest.getMaxPlaces() != null &&
                updateRequest.getMaxPlaces() < updatedEvent.registrationList().size()) {
            throw new BadRequestException(
                    "Количество регистраций превышает новое максимальное количество мест");
        }
    }
}
