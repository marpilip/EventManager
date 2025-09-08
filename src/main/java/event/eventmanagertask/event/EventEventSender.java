package event.eventmanagertask.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventEventSender {
    private static final Logger logger = LoggerFactory.getLogger(EventEventSender.class);
    @Value("${kafka.topics.events:events}")
    private String eventsTopic;

    private final KafkaTemplate<Long, EventKafkaEvent> kafkaTemplate;

    public EventEventSender(
            @Qualifier("eventKafkaTemplate")
            KafkaTemplate<Long, EventKafkaEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(EventKafkaEvent event) {
        logger.info("Sending event: " + event.toString());

        var result = kafkaTemplate.send(
                eventsTopic,
                event.eventId(),
                event
        );

        result.thenAccept(sendResult ->
                logger.info("Successfully sent event: " + event)
        );
    }
}
