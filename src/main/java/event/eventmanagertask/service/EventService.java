package event.eventmanagertask.service;

import event.eventmanagertask.dto.EventCreateRequestDto;
import event.eventmanagertask.dto.EventSearchRequestDto;
import event.eventmanagertask.dto.EventUpdateRequestDto;
import event.eventmanagertask.dto.LocationDto;
import event.eventmanagertask.entity.EventEntity;
import event.eventmanagertask.entity.EventStatus;
import event.eventmanagertask.entity.RegistrationEntity;
import event.eventmanagertask.event.EventEventSender;
import event.eventmanagertask.event.EventKafkaEvent;
import event.eventmanagertask.event.EventType;
import event.eventmanagertask.event.KafkaEventChangeSender;
import event.eventmanagertask.event.fields.EventChangeMessage;
import event.eventmanagertask.event.fields.EventFieldChange;
import event.eventmanagertask.exception.ForbiddenException;
import event.eventmanagertask.mapper.EventDtoMapper;
import event.eventmanagertask.mapper.EventEntityMapper;
import event.eventmanagertask.model.Event;
import event.eventmanagertask.model.Role;
import event.eventmanagertask.model.User;
import event.eventmanagertask.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventService {
    private static Logger logger = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final AuthenticationService authenticationService;
    private final EventEntityMapper eventEntityMapper;
    private final LocationService locationService;
    private final EventDtoMapper eventDtoMapper;
    private final EventEventSender eventEventSender;
    private final KafkaEventChangeSender eventChangeSender;

    public EventService(EventRepository eventRepository, AuthenticationService authenticationService,
                        EventEntityMapper eventEntityMapper, LocationService locationService, EventDtoMapper eventDtoMapper,
                        EventEventSender eventEventSender, KafkaEventChangeSender eventChangeSender) {
        this.eventRepository = eventRepository;
        this.authenticationService = authenticationService;
        this.eventEntityMapper = eventEntityMapper;
        this.locationService = locationService;
        this.eventDtoMapper = eventDtoMapper;
        this.eventEventSender = eventEventSender;
        this.eventChangeSender = eventChangeSender;
    }

    public Event createEvent(EventCreateRequestDto createRequest) throws BadRequestException {
        LocationDto location = locationService.getLocationById(createRequest.getLocationId());

        if (location.getCapacity() < createRequest.getMaxPlaces()) {
            throw new BadRequestException("Максимальное количество мест превышает вместимость локации");
        }

        authenticationService.getCurrentAuthenticatedUserOrThrow();

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

        Event savedEvent = eventEntityMapper.toDomain(eventEntity);

        eventEventSender.send(
                new EventKafkaEvent(
                        savedEvent.id(),
                        EventType.CREATED,
                        savedEvent
                )
        );

        return savedEvent;
    }

    public Event getEventById(Long eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Мероприятие не найдено"));

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

        eventEventSender.send(
                new EventKafkaEvent(
                        eventId,
                        EventType.REMOVED,
                        eventToCancel
                )
        );

        eventRepository.save(event);
    }

    public Event updateEvent(Long eventId, @Valid EventUpdateRequestDto updateRequest) throws BadRequestException {
        logger.info("Trying update event with ID: {}", eventId);

        EventEntity existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    logger.warn("Event not found with ID: {}", eventId);
                    return new EntityNotFoundException("Мероприятие не найдено");
                });

        User currentUser = authenticationService.getCurrentAuthenticatedUserOrThrow();

        if (currentUser.role() != Role.ADMIN && !existingEvent.getOwnerId().equals(currentUser.id())) {
            logger.warn("User {} doesn't have permission to update event {}", currentUser.id(), eventId);
            throw new ForbiddenException("Недостаточно прав для обновления мероприятия");
        }

        if (!existingEvent.getStatus().equals(EventStatus.WAIT_START)) {
            logger.warn("Cannot update event {} in status: {}", eventId, existingEvent.getStatus());
            throw new BadRequestException("Нельзя изменять мероприятие в статусе: " + existingEvent.getStatus());
        }

        EventEntity originalEvent = copyEventEntity(existingEvent);

        validateEventUpdate(eventEntityMapper.toDomain(existingEvent), updateRequest);
        applyUpdates(existingEvent, updateRequest);

        EventEntity updatedEvent = eventRepository.save(existingEvent);
        logger.info("Event {} successfully updated", eventId);

        sendEventChangeNotification(originalEvent, updatedEvent, currentUser.id());

        return eventEntityMapper.toDomain(updatedEvent);
    }

    private EventEntity copyEventEntity(EventEntity original) {
        EventEntity copy = new EventEntity();
        copy.setId(original.getId());
        copy.setName(original.getName());
        copy.setOwnerId(original.getOwnerId());
        copy.setMaxPlaces(original.getMaxPlaces());
        copy.setDate(original.getDate());
        copy.setCost(original.getCost());
        copy.setDuration(original.getDuration());
        copy.setLocationId(original.getLocationId());
        copy.setStatus(original.getStatus());

        return copy;
    }

    private void applyUpdates(EventEntity event, EventUpdateRequestDto requestDto) {
        if (requestDto.getName() != null) {
            event.setName(requestDto.getName());
        }
        if (requestDto.getMaxPlaces() != null) {
            event.setMaxPlaces(requestDto.getMaxPlaces());
        }
        if (requestDto.getDate() != null) {
            event.setDate(requestDto.getDate());
        }
        if (requestDto.getCost() != null) {
            event.setCost(requestDto.getCost());
        }
        if (requestDto.getDuration() != null) {
            event.setDuration(requestDto.getDuration());
        }
        if (requestDto.getLocationId() != null) {
            event.setLocationId(requestDto.getLocationId());
        }
    }

    private void sendEventChangeNotification(EventEntity originalEvent,
                                             EventEntity updatedEvent,
                                             Long changedByUserId) {

        List<Long> subscribers = getEventSubscribers(originalEvent.getId(), changedByUserId);

        EventChangeMessage message = new EventChangeMessage();
        message.setEventId(originalEvent.getId());
        message.setUsers(subscribers);
        message.setOwnerId(originalEvent.getOwnerId());
        message.setChangedById(changedByUserId);

        populateFieldChanges(message, originalEvent, updatedEvent);

        if (hasChanges(message)) {
            try {
                eventChangeSender.sendEventChange(message);
                logger.debug("Sent event change notification for eventId: {}", originalEvent.getId());
            } catch (Exception e) {
                logger.error("Failed to send event change notification: {}", e.getMessage(), e);
            }
        }
    }

    private boolean hasChanges(EventChangeMessage message) {
        return message.getName() != null || message.getMaxPlaces() != null ||
                message.getDate() != null || message.getCost() != null ||
                message.getDuration() != null || message.getLocationId() != null ||
                message.getStatus() != null;
    }

    private void populateFieldChanges(EventChangeMessage message,
                                      EventEntity original,
                                      EventEntity updated) {
        if (!original.getName().equals(updated.getName())) {
            message.setName(new EventFieldChange<>(original.getName(), updated.getName()));
        }

        if (!original.getMaxPlaces().equals(updated.getMaxPlaces())) {
            message.setMaxPlaces(new EventFieldChange<>(original.getMaxPlaces(), updated.getMaxPlaces()));
        }

        if (!original.getDate().equals(updated.getDate())) {
            message.setDate(new EventFieldChange<>(original.getDate(), updated.getDate()));
        }

        if (original.getCost() != updated.getCost()) {
            message.setCost(new EventFieldChange<>(
                    BigDecimal.valueOf(original.getCost()),
                    BigDecimal.valueOf(updated.getCost())
            ));
        }

        if (!original.getDuration().equals(updated.getDuration())) {
            message.setDuration(new EventFieldChange<>(original.getDuration(), updated.getDuration()));
        }

        if (!original.getLocationId().equals(updated.getLocationId())) {
            message.setLocationId(new EventFieldChange<>(original.getLocationId().intValue(), updated.getLocationId().intValue()));
        }

        if (!original.getStatus().equals(updated.getStatus())) {
            message.setStatus(new EventFieldChange<>(original.getStatus(), updated.getStatus()));
        }
    }

    public List<Event> getCurrentUserEvents() {
        var currentUser = authenticationService.getCurrentAuthenticatedUserOrThrow();
        var userEvents = eventRepository.findByUserIdRegistrations(currentUser.id());

        return userEvents.stream()
                .map(eventEntityMapper::toDomain)
                .toList();
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

    public List<Event> getAllEvents() {
        User currentUser = authenticationService.getCurrentAuthenticatedUserOrThrow();
        List<EventEntity> userEvents = eventRepository.findByOwnerId(currentUser.id());

        return userEvents.stream()
                .map(eventEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Event> searchEvents(EventSearchRequestDto searchRequest) {

        List<EventEntity> events = eventRepository.searchEvents(
                searchRequest.name(),
                searchRequest.placesMin(),
                searchRequest.placesMax(),
                searchRequest.dateStartAfter(),
                searchRequest.dateStartBefore(),
                searchRequest.costMin(),
                searchRequest.costMax(),
                searchRequest.durationMin(),
                searchRequest.durationMax(),
                searchRequest.locationId(),
                searchRequest.eventStatus()
        );

        return events.stream()
                .map(eventEntityMapper::toDomain)
                .collect(Collectors.toList());
    }


    private List<Long> getEventSubscribers(Long eventId, Long changedByUserId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено"));

        Set<Long> subscribers = new HashSet<>();

        if (!event.getOwnerId().equals(changedByUserId)) {
            subscribers.add(event.getOwnerId());
        }

        if (event.getRegistrationList() != null) {
            for (RegistrationEntity registration : event.getRegistrationList()) {
                Long userId = registration.getUserId();
                if (!userId.equals(changedByUserId)) {
                    subscribers.add(userId);
                }
            }
        }

        return new ArrayList<>(subscribers);
    }
}
