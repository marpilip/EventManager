package event.eventmanagertask.service;

import event.eventmanagertask.entity.EventEntity;
import event.eventmanagertask.entity.EventStatus;
import event.eventmanagertask.entity.RegistrationEntity;
import event.eventmanagertask.model.Event;
import event.eventmanagertask.model.User;
import event.eventmanagertask.repository.EventRepository;
import event.eventmanagertask.repository.RegistrationRepository;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RegistrationService {
    private static Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final AuthenticationService authenticationService;

    public RegistrationService(RegistrationRepository registrationRepository, EventRepository eventRepository,
                               EventService eventService, AuthenticationService authenticationService) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.eventService = eventService;
        this.authenticationService = authenticationService;
    }

    public void registerUserOnEvent(User user, Long eventId) throws BadRequestException {
        Event event = eventService.getEventById(eventId);

        Optional<RegistrationEntity> registration = registrationRepository.findByUserIdAndEventId(user.id(), eventId);

        if (registration.isPresent()) {
            throw new IllegalStateException("Пользователь уже зарегистрирован");
        }

        if (!event.status().equals(EventStatus.WAIT_START)) {
            throw new IllegalStateException("Невозможно зарегистрироваться на мероприятие со статусом: "
                    + event.status());
        }

        if (event.registrationList().size() >= event.maxPlaces()) {
            throw new BadRequestException("Не хватает места на мероприятии");
        }

        registrationRepository.save(
                new RegistrationEntity(
                        null,
                        user.id(),
                        eventRepository.findById(eventId).orElseThrow()
                )
        );
    }

    public void cancelUserRegistration(Long eventId) throws BadRequestException {
        User currentUser = authenticationService.getCurrentAuthenticatedUserOrThrow();

        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено"));

        if (event.getDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Нельзя отменить регистрацию на начавшееся мероприятие");
        }

        RegistrationEntity registration = registrationRepository.findByUserIdAndEventId(currentUser.id(), eventId)
                .orElseThrow(() -> new IllegalArgumentException("Регистрация не найдена"));

        registrationRepository.delete(registration);
    }

    public List<Long> getUserEventRegistrations() {
        User currentUser = authenticationService.getCurrentAuthenticatedUserOrThrow();
        List<RegistrationEntity> registrations = registrationRepository.findByUserId(currentUser.id());

        return registrations.stream()
                .map(registration -> registration.getEvent().getId())
                .toList();
    }

    public List<Long> getEventRegistrations(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new IllegalStateException("Мероприятие не найдено");
        }

        List<RegistrationEntity> registrations = registrationRepository.findByEventId(eventId);
        return registrations.stream()
                .map(RegistrationEntity::getUserId)
                .toList();
    }

    public boolean isUserRegistered(Long eventId, Long userId) {
        if (!eventRepository.existsById(eventId)) {
            throw new IllegalStateException("Мероприятие не найдено");
        }
        return registrationRepository.existsByUserIdAndEventId(userId, eventId);
    }
}
