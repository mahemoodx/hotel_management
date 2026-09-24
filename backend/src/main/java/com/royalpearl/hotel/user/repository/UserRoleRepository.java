package com.royalpearl.hotel.user.repository;

import com.royalpearl.hotel.user.entity.AppRole;
import com.royalpearl.hotel.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    boolean existsByUserIdAndRole(UUID userId, AppRole role);

    Optional<UserRole> findByUserIdAndRole(UUID userId, AppRole role);
}
