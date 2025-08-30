package event.eventmanagertask.repository;

import event.eventmanagertask.entity.RegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<RegistrationEntity, Long> {
    Optional<RegistrationEntity> findByUserIdAndEventId(Long userId, Long eventId);

    List<RegistrationEntity> findByEventId(Long eventId);

    List<RegistrationEntity> findByUserId(Long userId);

    @Query("SELECT COUNT(r) FROM RegistrationEntity r WHERE r.event.id = :eventId")
    Integer countByEventId(@Param("eventId") Long eventId);

    void deleteByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}
