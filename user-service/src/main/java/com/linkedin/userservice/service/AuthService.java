package com.linkedin.userservice.service;

import com.linkedin.userservice.dto.AuthResponse;
import com.linkedin.userservice.dto.RegisterRequest;
import com.linkedin.userservice.entity.User;
import com.linkedin.userservice.entity.UserRole;
import com.linkedin.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

   private final UserRepository userRepository;

   private final BCryptPasswordEncoder passwordEncoder;

   public AuthResponse register(RegisterRequest request) {
      log.info("Registering new user: {}", request.getEmail());

      if(userRepository.existsByEmail(request.getEmail())){
         throw new RuntimeException("Email already exists" + request.getEmail());
      }

      User user = User.builder()
              .firstName(request.getFirstName())
              .lastName(request.getLastName())
              .email(request.getEmail())
              .headLine(request.getHeadLine())
              .location(request.getLocation())
              .password(passwordEncoder.encode(request.getPassword()))
              .role(UserRole.NORMAL_USER)
              .build(); //closed

      User savedUser = userRepository.save(user);
      log.info("User registered successfully: {}", savedUser.getId());

      //publish event to kafka - why ?

   }
}
