package com.linkedin.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
   @NotBlank(message = "E-mail is required for registration")
   @Email(message = "Invalid Email Format")
   private String email;

   @NotBlank
   private String password;
}
