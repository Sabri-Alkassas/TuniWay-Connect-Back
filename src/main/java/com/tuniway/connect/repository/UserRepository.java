package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.model.entity.AccountStatus;
import com.tuniway.connect.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(Role role);
    long countByRoleIn(Collection<Role> roles);
    long countByRoleInAndStatus(Collection<Role> roles, AccountStatus status);
}
