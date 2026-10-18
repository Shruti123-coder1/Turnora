package com.turnora.repository;

import com.turnora.model.entity.Ticket;
import com.turnora.model.enums.TicketStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    long countByServiceIdAndQueueDateAndStatus(Long serviceId, LocalDate queueDate, TicketStatus status);

    boolean existsByServiceId(Long serviceId);

    boolean existsByCounterId(Long counterId);

    Optional<Ticket> findByTokenNumberAndQueueDate(String tokenNumber, LocalDate queueDate);

    List<Ticket> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Does this phone number already have a live token in this office today?
    boolean existsByServiceIdAndQueueDateAndPhoneAndStatusIn(Long serviceId, LocalDate queueDate,
                                                             String phone, Collection<TicketStatus> statuses);

    // Highest token number given today for this office prefix (0 if none), for example prefix pattern "F-%"
    @Query("select coalesce(max(t.sequenceNo), 0) from Ticket t "
            + "where t.queueDate = :date and t.tokenNumber like :tokenPattern")
    Integer maxSequence(@Param("date") LocalDate date, @Param("tokenPattern") String tokenPattern);

    // How many waiting people are before this ticket (higher priority first, then first come first served)
    @Query("select count(t) from Ticket t "
            + "where t.service.id = :serviceId and t.queueDate = :date and t.status = :status and t.id <> :id "
            + "and (t.priorityWeight > :weight "
            + "  or (t.priorityWeight = :weight and t.createdAt < :createdAt) "
            + "  or (t.priorityWeight = :weight and t.createdAt = :createdAt and t.id < :id))")
    long countAhead(@Param("serviceId") Long serviceId,
                    @Param("date") LocalDate date,
                    @Param("status") TicketStatus status,
                    @Param("weight") int weight,
                    @Param("createdAt") LocalDateTime createdAt,
                    @Param("id") Long id);

    // The waiting line of one office, in serving order
    @Query("select t from Ticket t "
            + "where t.service.id = :serviceId and t.queueDate = :date and t.status = :status "
            + "order by t.priorityWeight desc, t.createdAt asc, t.id asc")
    List<Ticket> findQueue(@Param("serviceId") Long serviceId,
                           @Param("date") LocalDate date,
                           @Param("status") TicketStatus status);

    // Call Next: finds the next person and LOCKS the row. SKIP LOCKED (-2) means that if another counter
    // has just locked the first person, this counter gets the next one. So two counters never get the same person.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select t from Ticket t "
            + "where t.service.id in :serviceIds and t.queueDate = :date and t.status = :status "
            + "order by t.priorityWeight desc, t.createdAt asc, t.id asc")
    List<Ticket> findNextForUpdate(@Param("serviceIds") Collection<Long> serviceIds,
                                   @Param("date") LocalDate date,
                                   @Param("status") TicketStatus status,
                                   Pageable pageable);

    // Latest calls of today (display board)
    List<Ticket> findByQueueDateAndCalledAtIsNotNullOrderByCalledAtDesc(LocalDate queueDate, Pageable pageable);
}
