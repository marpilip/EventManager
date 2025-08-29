package event.eventmanagertask.controller;

import event.eventmanagertask.dto.EventCreateRequestDto;
import event.eventmanagertask.dto.EventDto;
import event.eventmanagertask.dto.EventSearchRequestDto;
import event.eventmanagertask.dto.EventUpdateRequestDto;
import event.eventmanagertask.mapper.EventDtoMapper;
import event.eventmanagertask.model.Event;
import event.eventmanagertask.service.EventService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    private final EventService eventService;
    private final EventDtoMapper eventDtoMapper;

    public EventController(EventService eventService, EventDtoMapper eventDtoMapper) {
        this.eventService = eventService;
        this.eventDtoMapper = eventDtoMapper;
    }

    @PostMapping
    public ResponseEntity<EventDto> createEvent(@Valid @RequestBody EventCreateRequestDto requestDto)
            throws BadRequestException {
        Event createdEvent = eventService.createEvent(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(eventDtoMapper.toDto(createdEvent));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEvent(@PathVariable Long eventId) {
        Event event = eventService.getEventById(eventId);

        return ResponseEntity.ok(eventDtoMapper.toDto(event));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long eventId,
                                                @Valid @RequestBody EventUpdateRequestDto requestDto)
            throws BadRequestException {
        Event updatedEvent = eventService.updateEvent(eventId, requestDto);

        return ResponseEntity.ok(eventDtoMapper.toDto(updatedEvent));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) throws BadRequestException {
        eventService.cancelEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    public ResponseEntity<List<EventDto>> searchEvents(
            @RequestBody EventSearchRequestDto searchRequest) {
        List<Event> events = eventService.searchEvents(searchRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(events.stream()
                        .map(eventDtoMapper::toDto)
                        .toList());
    }

    @GetMapping("/my")
    public ResponseEntity<List<EventDto>> getAllUserEvents() {
        List<Event> events = eventService.getAllEvents();

        return ResponseEntity.status(HttpStatus.OK)
                .body(events.stream()
                        .map(eventDtoMapper::toDto)
                        .toList());
    }
}
