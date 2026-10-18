package com.turnora.repository;

import com.turnora.model.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {

    List<ServiceType> findByActiveTrueOrderByIdAsc();

    List<ServiceType> findAllByOrderByIdAsc();

    Optional<ServiceType> findByPrefix(String prefix);

    boolean existsByPrefix(String prefix);

    boolean existsByName(String name);
}
