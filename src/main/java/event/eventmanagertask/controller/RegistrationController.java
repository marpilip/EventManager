package event.eventmanagertask.controller;

import event.eventmanagertask.model.User;
import event.eventmanagertask.service.AuthenticationService;
import event.eventmanagertask.service.RegistrationService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/registrations")
public class RegistrationController {
    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;

    public RegistrationController(RegistrationService registrationService,
                                  AuthenticationService authenticationService) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
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
    public ResponseEntity<List<Long>> getUserRegistrations() {
        List<Long> eventIds = registrationService.getUserEventRegistrations();

        return ResponseEntity.ok(eventIds);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Long>> getEventRegistrations(@PathVariable Long eventId) {
        List<Long> userIds = registrationService.getEventRegistrations(eventId);

        return ResponseEntity.ok(userIds);
    }

    @GetMapping("/check/{eventId}/user/{userId}")
    public ResponseEntity<Boolean> checkSpecificUserRegistration(
            @PathVariable Long eventId,
            @PathVariable Long userId) {
        boolean isRegistered = registrationService.isUserRegistered(eventId, userId);
        return ResponseEntity.ok(isRegistered);
    }


}
