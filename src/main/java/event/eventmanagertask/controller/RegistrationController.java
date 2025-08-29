package event.eventmanagertask.controller;

import event.eventmanagertask.dto.EventDto;
import event.eventmanagertask.mapper.EventDtoMapper;
import event.eventmanagertask.model.Event;
import event.eventmanagertask.model.User;
import event.eventmanagertask.service.AuthenticationService;
import event.eventmanagertask.service.RegistrationService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("events/registrations")
public class RegistrationController {
    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;
    private final EventDtoMapper eventDtoMapper;

    public RegistrationController(RegistrationService registrationService,
                                  AuthenticationService authenticationService, EventDtoMapper eventDtoMapper) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
        this.eventDtoMapper = eventDtoMapper;
    }

    @PostMapping("/{eventId}")
    public ResponseEntity<Void> registerForEvent(@PathVariable Long eventId) throws BadRequestException {
        User user = authenticationService.getCurrentAuthenticatedUserOrThrow();
        registrationService.registerUserOnEvent(user, eventId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cancel/{eventId}")
    public ResponseEntity<Void> cancelRegistration(@PathVariable Long eventId) throws BadRequestException {
        registrationService.cancelUserRegistration(eventId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<EventDto>> getUserRegistrations() {
        List<Event> eventIds = registrationService.getUserEventRegistrations();

        return ResponseEntity.status(HttpStatus.OK)
                .body(eventIds.stream()
                        .map(eventDtoMapper::toDto)
                        .toList()
                );
    }
}
