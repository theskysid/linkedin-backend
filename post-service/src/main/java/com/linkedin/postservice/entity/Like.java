package com.linkedin.postservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "likes",
                  uniqueConstraints = @UniqueConstraint(
                          columnNames = {"post_Id", "user_Id"}))

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Builder
public class Like {

   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   private String id;

   @Column(name = "post_id", nullable = false)
   private String postId;

   @Column(name = "user_id", nullable = false)
   private String userId;

   @CreationTimestamp
   private LocalDateTime createdAt;

}
