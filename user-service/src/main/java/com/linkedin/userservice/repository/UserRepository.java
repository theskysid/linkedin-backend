package com.linkedin.userservice.repository;

import com.linkedin.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
   boolean existsByEmail(String email);
}
