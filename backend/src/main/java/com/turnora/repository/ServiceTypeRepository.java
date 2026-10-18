package com.turnora.repository;

import com.turnora.model.entity.ServiceType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {

    List<ServiceType> findByActiveTrueOrderByIdAsc();

    List<ServiceType> findAllByOrderByIdAsc();

    Optional<ServiceType> findByPrefix(String prefix);

    boolean existsByPrefix(String prefix);

    boolean existsByName(String name);

    // Locks the office row, so two people joining at the same moment never get the same token number
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ServiceType s where s.id = :id")
    Optional<ServiceType> findByIdForUpdate(@Param("id") Long id);
}
