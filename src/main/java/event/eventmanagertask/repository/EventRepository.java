package event.eventmanagertask.repository;

import event.eventmanagertask.entity.EventEntity;
import event.eventmanagertask.entity.EventStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findByOwnerId(Long ownerId);

    @Query("SELECT e FROM EventEntity e WHERE e.status IN :statuses")
    List<EventEntity> findByStatusIn(@Param("statuses") List<EventStatus> statuses);

    @Query("SELECT e FROM EventEntity e JOIN e.registrationList r WHERE r.userId = :userId")
    List<EventEntity> findByUserIdRegistrations(@Param("userId") Long userId);

    @Query(value = "SELECT e.id FROM events e WHERE e.date < NOW() AND e.status = 'WAIT_START'", nativeQuery = true)
    List<Long> findStartedEventsWithStatus(@Param("status") EventStatus status);

    @Query(value = "SELECT e.id FROM events e WHERE e.date + INTERVAL '1 minute' * e.duration < NOW() " +
            "AND e.status = 'STARTED'", nativeQuery = true)
    List<Long> findEndedEventsWithStatus(@Param("status") EventStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE EventEntity e SET e.status = :status WHERE e.id = :eventId")
    void changeEventStatus(@Param("eventId") Long eventId, @Param("status") EventStatus status);

    @Query("""
            SELECT e FROM EventEntity e 
            WHERE (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:placesMin IS NULL OR e.maxPlaces >= :placesMin)
            AND (:placesMax IS NULL OR e.maxPlaces <= :placesMax)
            AND (:dateStartAfter IS NULL OR e.date >= :dateStartAfter)
            AND (:dateStartBefore IS NULL OR e.date <= :dateStartBefore)
            AND (:costMin IS NULL OR e.cost >= :costMin)
            AND (:costMax IS NULL OR e.cost <= :costMax)
            AND (:durationMin IS NULL OR e.duration >= :durationMin)
            AND (:durationMax IS NULL OR e.duration <= :durationMax)
            AND (:locationId IS NULL OR e.locationId = :locationId)
            AND (:eventStatus IS NULL OR e.status = :eventStatus)
            """)
    List<EventEntity> searchEvents(
            @Param("name") String name,
            @Param("placesMin") Integer placesMin,
            @Param("placesMax") Integer placesMax,
            @Param("dateStartAfter") LocalDateTime dateStartAfter,
            @Param("dateStartBefore") LocalDateTime dateStartBefore,
            @Param("costMin") BigDecimal costMin,
            @Param("costMax") BigDecimal costMax,
            @Param("durationMin") Integer durationMin,
            @Param("durationMax") Integer durationMax,
            @Param("locationId") Long locationId,
            @Param("eventStatus") EventStatus eventStatus
    );
}
