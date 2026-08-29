package com.linkedin.userservice.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

   @NotBlank(message = "E-mail is required for registration")
   @Email(message = "Invalid Email Format")
   private String email;

   @NotBlank
   @Size(min = 6, message = "Password must be at least 6 characters")
   private String password;



   @NotBlank(message = "First name is required for registration")
   private String firstName;

   private String lastName;
   private String headLine;
   private String about;
   private String location;
}
