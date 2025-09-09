package event.eventmanagertask.event;

import event.eventmanagertask.event.fields.EventChangeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventChangeSender {
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventChangeSender.class);

    @Value("${kafka.topics.event-changes:events-changes}")
    private String topic;

    private final KafkaTemplate<String, EventChangeMessage> kafkaTemplate;

    public KafkaEventChangeSender(
            @Qualifier("eventChangeKafkaTemplate")
            KafkaTemplate<String, EventChangeMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEventChange(EventChangeMessage message) {
        logger.info("Sending changed event: " + message.toString());

        var result = kafkaTemplate.send(topic, message);

        result.thenAccept(sendResult ->
                logger.info("Successfully sent event: " + message)
        );
    }
}
