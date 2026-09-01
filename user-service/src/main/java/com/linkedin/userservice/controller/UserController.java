package com.linkedin.userservice.controller;

import com.linkedin.userservice.dto.UserResponse;
import com.linkedin.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//all the endpoints are jwt secured
@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

   private final UserService userService;

   /*
   * Get user profile
   * x-user-id = requesting user from gateway
   * userId in path = target user to fetch
   * @return
   * */
   @GetMapping("/{userId}")
   public ResponseEntity<UserResponse> getUserProfile(
           @PathVariable String userId,
           @RequestHeader("X-User-Id") String requestingUserId
   ){

      log.info("Received request for user id {} from id {}", userId, requestingUserId);

      return ResponseEntity.ok(userService.getUserProfile(userId));
   }

   /**
    * Update user profile
    * user can only update their own profile
    * @param userId = target user to update
    * @param requestingUserId = requesting user from gateway
    * @return
    * */
   @PutMapping("/{userId}/profile")
   public ResponseEntity<UserResponse> updateProfile(
           @PathVariable String userId,
           @RequestHeader("X-User-Id") String requestingUserId,
           @RequestBody UserResponse request
   ){
      if (!userId.equals(requestingUserId)) {
         return ResponseEntity.status(403).build();
      }

      return ResponseEntity.ok(userService.updateProfile(userId, request));
   }

   //PENDING ENDPOINT --connection acception and rejection

   /*
   * Send Connection request
   * requester id comes from the x user id header - already validated
   * @Param targetUserId
   * @Param requestingUserId
   * @return
   * */
   @PostMapping("/{targetUserId}/connect")
   public ResponseEntity<String> sendConnectionRequest(
           @PathVariable String targetUserId,
           @RequestHeader("X-User-Id") String requestingUserId
   ){
      return ResponseEntity.ok(userService.sendConnectionRequest(targetUserId, requestingUserId));
   }

   @PutMapping("/connection/{connectionId}/accept")
   public ResponseEntity<String> acceptConnection(
           @PathVariable String connectionId,
           @RequestHeader("X-User-Id") String requestingUserId
   ){
      return ResponseEntity.ok(userService.acceptConnectionRequest(connectionId));
   }


   //get all the connections
   @GetMapping("/{userId}/connections")
   public ResponseEntity<List<UserResponse>> getConnection(@PathVariable String userId){
      return ResponseEntity.ok(userService.getConnections(userId));
   }
}
