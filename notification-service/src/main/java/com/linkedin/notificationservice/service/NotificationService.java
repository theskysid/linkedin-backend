package com.linkedin.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

   /**
    * Consume user.created event
    * send welcome notification
    */
   @KafkaListener(topics = "user.created")
   public void consumeUserCreated(
           @Payload Map<String, Object> payload
           ){
      try{

         String userId = payload.get("userId").toString();
         String firstName = payload.get("firstName").toString();

         sendNotification(userId,
                 "Welcome to LinkedIn!",
                 String.format(
                         "Welcome %s Your account has been created" + "Start connecting with professionals.", firstName
                 )
         );

      } catch (Exception e){
         log.error("Error while sending notification: {}", e.getMessage());
      }
   }

   /**
    * consume connection.requested event
    * Notify receiver about connection request
    */
   @KafkaListener(topics = "connection.requested")
   public void consumeConnectionRequest(
           @Payload Map<String, Object> payload
           ){
      try{

         String receiverId = (String) payload.get("receiverId");
         String requesterId = (String) payload.get("requesterId");

         sendNotification(receiverId,
                 "New Connection Request",
                 String.format("user %s want to connect with you" , requesterId)
         );
      } catch (Exception e){
         log.error("Error while sending connection request notification: {}", e.getMessage());
      }
   }


   /**
    * Consume consume.accepted event
    * notify the requester that connection is accepted
    */
   @KafkaListener(topics = "connection.accepted")
   public void consumeConnectionAccepted(@Payload Map<String, Object> payload){
      try{
         String receiverId = (String) payload.get("receiverId");
         String requesterId = (String) payload.get("requesterId");

         sendNotification(receiverId,
                 "Connection Accepted",
                    String.format("User %s has accepted your connection request." + "You are now connected" , requesterId)
         );
      } catch (Exception e){
         log.error("Error while sending connection accepted notification: {}", e.getMessage());
      }
   }

   @KafkaListener(topics = "post.liked")
   public void consumePostLiked(@Payload Map<String, Object> payload){
      try{
         String authorId = (String) payload.get("authorId");
         String userId = (String) payload.get("userId");
         String postId = (String) payload.get("postId");

         sendNotification(authorId,
                 "Someone liked your post",
                 String.format("User %s liked your post %s", userId, postId));
      } catch (Exception e){
         log.error("Error while sending post liked notification: {}", e.getMessage());
      }
   }

   @KafkaListener(topics = "post.commented")
   public void consumePostCommented(@Payload Map<String, Object> payload){
      try{
         String postAuthorId = (String) payload.get("postAuthorId");
         String commenterId = (String) payload.get("commenterId");
         String postId = (String) payload.get("postId");

         sendNotification(
                 postAuthorId,
                 "New comment on the post",
                 String.format("User %s has commented your post %s", commenterId, postId)
         );
      } catch (Exception e){
         log.error("Error while sending post commented notification: {}", e.getMessage());
      }
   }

   private void sendNotification(
           String userId, String title, String message
   ){
      //testing will implement later
         log.info("------------------------------------------");
         log.info("Notification sent");
         log.info("to user:{}", userId);
         log.info("title: {}", title);
         log.info("message: {}", message);
         log.info("------------------------------------------");
   }
}
