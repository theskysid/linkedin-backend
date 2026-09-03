package com.linkedin.userservice.service;

import com.linkedin.userservice.dto.AuthResponse;
import com.linkedin.userservice.dto.LoginRequest;
import com.linkedin.userservice.dto.RegisterRequest;
import com.linkedin.userservice.entity.User;
import com.linkedin.userservice.entity.UserRole;
import com.linkedin.userservice.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

   private final UserRepository userRepository;

   private final BCryptPasswordEncoder passwordEncoder;

   private final KafkaTemplate<String, Object> kafkaTemplate;

   private static final String USER_CREATED_TOPIC = "user.created";

   @Value("${jwt.secret}")
   private String secretKey;

   @Value("${jwt.expiration}")
   private long jwtExpiration;

   @Value("${jwt.refresh-expiration}")
   private long refreshExpiration;

   public AuthResponse register(RegisterRequest request) {
      log.info("Registering new user: {}", request.getEmail());

      if (userRepository.existsByEmail(request.getEmail())) {
         throw new RuntimeException("Email already exists" + request.getEmail());
      }

      User user = User.builder() // builds the user using the @Builder in the entity
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .headLine(request.getHeadLine())
            .location(request.getLocation())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(UserRole.NORMAL_USER)
            .build(); // closed

      User savedUser = userRepository.save(user);
      log.info("User registered successfully: {}", savedUser.getId());

      /*
       * publish event to kafka - why ?
       * This is to notify other services that a new user has been registered.
       * so that search service can consume this and indexes user
       */

      Map<String, Object> userCreatedEvent = new HashMap<>();
      userCreatedEvent.put("userId", savedUser.getId());
      userCreatedEvent.put("email", savedUser.getEmail());
      userCreatedEvent.put("firstName", savedUser.getFirstName());
      userCreatedEvent.put("lastName", savedUser.getLastName());
      userCreatedEvent.put("headLine", savedUser.getHeadLine());
      userCreatedEvent.put("location", savedUser.getLocation());
      // Publish the event to Kafka
      kafkaTemplate.send(USER_CREATED_TOPIC, savedUser.getId(), userCreatedEvent);

      log.info("user.created event published: {}", savedUser.getId());

      String token = generateToken(savedUser.getId(), savedUser.getEmail());
      return buildAuthResponse(savedUser, token);
   }

   public AuthResponse login(LoginRequest request) {
      log.info("Logining: {}", request.getEmail());

      User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found with this email: " + request.getEmail()));

      // bcrypt verify - compare raw password with stored hash
      if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
         throw new RuntimeException("Invalid Credentials");
      }

      log.info("Login successful: {}", user.getId());

      // means now user is logged in need to generate the jwt token

      String token = generateToken(user.getId(), user.getEmail());

      return buildAuthResponse(user, token);
   }

   /*
    * Generate access token
    * 
    * @param user id
    * 
    * @param email
    * 
    * @return
    */
   private String generateToken(String userId, String email) {
      return Jwts.builder()
            .claim("userId", userId)
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // 24 hours
            .signWith(getSigninKey(), SignatureAlgorithm.HS256) //
            .compact();
   }

   private Key getSigninKey() {
      byte[] keyByte = Decoders.BASE64.decode(secretKey);
      return Keys.hmacShaKeyFor(keyByte);
   }

   private AuthResponse buildAuthResponse(User user, String token) {
      AuthResponse response = new AuthResponse();
      response.setAccessToken(token);
      response.setRefreshToken(
            generateRefreshToken(user.getId()));
      response.setUserId(user.getId());
      response.setEmail(user.getEmail());
      response.setFirstName(user.getFirstName());
      response.setLastName(user.getLastName());

      return response;
   }

   /*
    * Generate refresh token
    * used to get a new access token when it expires
    * server validates and return new access token
    * client sends refresh token to /auth/refresh endpoint
    * 
    * @param user id
    * 
    * @return
    */
   private String generateRefreshToken(String userId) {
      return Jwts.builder()
            .claim("userId", userId)
            .setSubject(userId)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(getSigninKey(), SignatureAlgorithm.HS256)
            .compact();
   }
}
