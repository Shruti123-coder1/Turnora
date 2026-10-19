package com.turnora.repository;

import com.turnora.model.entity.User;
import com.turnora.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleOrderByIdAsc(Role role);

    List<User> findByRoleAndActiveTrueOrderByIdAsc(Role role);
}
