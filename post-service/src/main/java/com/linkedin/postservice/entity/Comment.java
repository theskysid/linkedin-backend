package com.linkedin.postservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "comments")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Builder
public class Comment {

   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   private String id;

   @Column(nullable = false)
   private String postId;

   @Column(nullable = false)
   private String authorId;

   @Column(nullable = false, columnDefinition = "TEXT")
   private String content;

   @CreationTimestamp
   private LocalDateTime  createdAt;
}
