package com.linkedin.userservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

   private String accessToken;
   private String refreshToken;
   private String userId;
   private String email;
   private String firstName;
   private String lastName;
   private String tokenType = "Bearer";
}
