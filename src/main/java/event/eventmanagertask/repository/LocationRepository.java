package event.eventmanagertask.repository;

import event.eventmanagertask.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
