package com.digitalstamp.repository;

import com.digitalstamp.entity.Role;
import com.digitalstamp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<User> findByQrToken(String qrToken);
    Page<User> findByRole(Role role, Pageable pageable);
    Page<User> findByRoleAndNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
            Role role1, String name, Role role2, String email, Pageable pageable);
    long countByRole(Role role);
    List<User> findByRole(Role role);
}
