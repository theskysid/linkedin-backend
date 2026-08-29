package com.linkedin.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   private String id;

   @Column(nullable = false, unique = true)
   private String email;

   @Column(nullable = false)
   private String password;

   @Column(nullable = false)
   private String firstName;

   @Column(nullable = false)
   private String lastName;
   private String headLine;
   private String about;
   private String location;
   private String profilePhotoUrl;
   private String coverPhotoUrl;

   @Enumerated(EnumType.STRING)
   private UserRole role;

   @ElementCollection //it is used to map a collection of basic or embeddable values to a separate database table
   @CollectionTable(name = "user_skills",
           joinColumns = @JoinColumn(name="user_id")) // a user can have multiple skill so we are creating a separate table to store this collection
   @Column(name = "skill")
   private List<String> skills = new ArrayList<>();

   private LocalDateTime  createdAt;

   private LocalDateTime updatedAt;
}
