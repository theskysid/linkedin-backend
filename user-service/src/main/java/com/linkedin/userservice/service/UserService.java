package com.linkedin.userservice.service;

import com.linkedin.userservice.dto.UserResponse;
import com.linkedin.userservice.entity.Connection;
import com.linkedin.userservice.entity.ConnectionStatus;
import com.linkedin.userservice.entity.User;
import com.linkedin.userservice.repository.ConnectionRepository;
import com.linkedin.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

   private final ConnectionRepository connectionRepository;
   private final UserRepository userRepository;
   private final KafkaTemplate<String, Object> kafkaTemplate;
   private static final String CONNECTION_REQUESTED_TOPIC = "connection.requested";
   private static final String CONNECTION_ACCEPTED_TOPIC = "connection.accepted";
   private static final String USER_UPDATED_TOPIC = "user.updated";
   private final S3Service s3Service;

   public UserResponse getUserProfile(String userId){
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
      return mapToResponse(user);
   }

   private UserResponse mapToResponse(User user){
      UserResponse response = new UserResponse();
      response.setId(user.getId());
      response.setEmail(user.getEmail());
      response.setFirstName(user.getFirstName());
      response.setLastName(user.getLastName());
      response.setHeadLine(user.getHeadLine());
      response.setProfilePhotoUrl(user.getProfilePhotoUrl());
      response.setCoverPhotoUrl(user.getCoverPhotoUrl());
      response.setRole(user.getRole());
      response.setSkills(user.getSkills());
      response.setCreatedAt(user.getCreatedAt());

      return response;
   }

   public String sendConnectionRequest(String receiverId, String requesterId){
      if (connectionRepository.existsByRequesterIdAndReceiverId(receiverId, requesterId)){
         throw new RuntimeException("Connection request already sent");
      }

      Connection connection = Connection.builder()
              .requesterId(requesterId)
              .receiverId(receiverId)
              .status(ConnectionStatus.PENDING)
              .build();
      connectionRepository.save(connection);

      //publish this event
      // You can use an event publisher here to notify other services

      Map<String, Object> connectionRequestedEvent = new HashMap<>();
      connectionRequestedEvent.put("requesterId", requesterId);
      connectionRequestedEvent.put("receiverId", receiverId);

      kafkaTemplate.send(CONNECTION_REQUESTED_TOPIC, requesterId, connectionRequestedEvent);

      log.info("Sent connection request for user id {} from id {}", requesterId, receiverId);

      return "Connection request sent";
   }

   public String acceptConnectionRequest(String connectionId){
      Connection connection = connectionRepository.findById(connectionId)
              .orElseThrow(
               () -> new RuntimeException("Connection not found with id " + connectionId)
               );
      connection.setStatus(ConnectionStatus.CONNECTED);
      connectionRepository.save(connection);

      //publish connection accepted event
      Map<String, Object> connectionAcceptedEvent = new HashMap<>();
      connectionAcceptedEvent.put("connectionId", connection.getRequesterId());
      connectionAcceptedEvent.put("requesterId", connection.getReceiverId());

      kafkaTemplate.send(CONNECTION_ACCEPTED_TOPIC, connection.getRequesterId(), connectionAcceptedEvent);

      log.info("Connection accepted for user id {} from id {} with connectionId as {}", connection.getRequesterId(), connection.getReceiverId(), connectionId);

      return "Connection request accepted";
   }

   public List<UserResponse> getConnections(String userId){
      List<Connection> connections = connectionRepository.findByRequesterIdAndStatus(userId, ConnectionStatus.CONNECTED);

      return connections.stream()
              .map(c -> getUserProfile(c.getReceiverId()))
              .collect(Collectors.toList());
   }

   public UserResponse updateProfile(String userId, UserResponse request){
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

      user.setHeadLine(request.getHeadLine());
      user.setAbout(request.getAbout());
      user.setLocation(request.getLocation());
      user.setSkills(request.getSkills());

      User savedUser = userRepository.save(user);

      //publish user.updated event

      Map<String, Object> userUpdatedEvent = new HashMap<>();
      userUpdatedEvent.put("userId", savedUser.getId());
      userUpdatedEvent.put("firstName", savedUser.getFirstName());
      userUpdatedEvent.put("lastName", savedUser.getLastName());
      userUpdatedEvent.put("headLine", savedUser.getHeadLine());
      userUpdatedEvent.put("about", savedUser.getAbout());
      userUpdatedEvent.put("location", savedUser.getLocation());
      userUpdatedEvent.put("skills", savedUser.getSkills());

      kafkaTemplate.send(USER_UPDATED_TOPIC, savedUser.getId(), userUpdatedEvent);

      log.info("user.updated for user id {} from id {}", savedUser.getId(), userId);

      return mapToResponse(savedUser);
   }

   public UserResponse uploadProfilePhoto(String userId, MultipartFile file){
      User user  = userRepository.findById(userId)
              .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

      String photoUrl = s3Service.uploadFile(file, "profiles/" + userId + "/avatar");

      user.setProfilePhotoUrl(photoUrl);
      User savedUser = userRepository.save(user);

      log.info("Profile photo uploaded for user id {}", savedUser.getId());
      return mapToResponse(savedUser);
   }
}
