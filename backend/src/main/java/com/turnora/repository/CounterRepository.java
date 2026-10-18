package com.turnora.repository;

import com.turnora.model.entity.Counter;
import com.turnora.model.enums.CounterStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CounterRepository extends JpaRepository<Counter, Long> {

    List<Counter> findAllByOrderByIdAsc();

    List<Counter> findByStaffIdOrderByIdAsc(Long staffId);

    Optional<Counter> findByStaffId(Long staffId);

    // Number of counters with this status that serve the given office
    @Query("select count(c) from Counter c join c.services s where s.id = :serviceId and c.status = :status")
    long countByServiceAndStatus(@Param("serviceId") Long serviceId, @Param("status") CounterStatus status);

    // Locks the counter row, so a double click on Call Next cannot give two tokens to one counter
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Counter c where c.id = :id")
    Optional<Counter> findByIdForUpdate(@Param("id") Long id);
}
