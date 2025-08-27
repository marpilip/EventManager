package event.eventmanagertask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventManagerTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventManagerTaskApplication.class, args);
    }

}
