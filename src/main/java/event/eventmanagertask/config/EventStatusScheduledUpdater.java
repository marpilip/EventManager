package event.eventmanagertask.config;

import event.eventmanagertask.entity.EventStatus;
import event.eventmanagertask.repository.EventRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventStatusScheduledUpdater {
    private final static Logger logger = LoggerFactory.getLogger(EventStatusScheduledUpdater.class);
    private final EventRepository eventRepository;

    public EventStatusScheduledUpdater(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Scheduled(cron = "${event.stats.cron:0 */5 * * * *}")
    @Transactional
    public void updateEventStatuses() {
        try {
            List<Long> startedEvents = eventRepository.findStartedEventsWithStatus(EventStatus.WAIT_START);

            startedEvents.forEach(eventId -> {
                eventRepository.changeEventStatus(eventId, EventStatus.STARTED);
                logger.debug("Статус мероприятия {} изменен на STARTED", eventId);
            });

            List<Long> endedEvents = eventRepository.findEndedEventsWithStatus(EventStatus.STARTED);
            endedEvents.forEach(eventId -> {
                eventRepository.changeEventStatus(eventId, EventStatus.FINISHED);
                logger.debug("Статус мероприятия {} изменен на FINISHED", eventId);
            });

            logger.info("Начатых мероприятий: {}, Завершенных: {}",
                    startedEvents.size(), endedEvents.size());

        } catch (Exception e) {
            logger.error("Ошибка в обработке статусов мероприятий ", e);
        }
    }
}
