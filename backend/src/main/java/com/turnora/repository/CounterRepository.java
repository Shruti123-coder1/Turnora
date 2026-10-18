package com.turnora.repository;

import com.turnora.model.entity.Counter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CounterRepository extends JpaRepository<Counter, Long> {

    List<Counter> findAllByOrderByIdAsc();

    List<Counter> findByStaffIdOrderByIdAsc(Long staffId);

    Optional<Counter> findByStaffId(Long staffId);
}