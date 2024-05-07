package com.auth.repository;

import com.auth.dto.UserDto;
import com.auth.entity.User;
import com.auth.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findFirstByEmail(String email);

    User findByRole(UserRole userRole);

    List<UserDto> findByNameContains(String name);
}
