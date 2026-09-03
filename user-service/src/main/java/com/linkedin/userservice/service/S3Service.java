package com.linkedin.userservice.service;

import com.linkedin.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import software.amazon.awssdk.core.sync.RequestBody;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

   private final UserRepository userRepository;

   @Value("${aws.s3.bucket-name}")
   private String bucketName;

   @Value("${aws.s3.region}")
   private String region;

   private final S3Client s3Client;

   public String uploadFile(MultipartFile file, String keyPrefix) {
      try {
         String key = keyPrefix + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

         PutObjectRequest request = PutObjectRequest.builder()
               .bucket(bucketName)
               .key(key)
               .contentType(file.getContentType())
               .build();

         s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

         String url = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;

         log.info("File Uploaded Successfully to s3: {} " + url);

         return url;
      } catch (Exception e) {

         throw new RuntimeException("Failed to upload file to s3", e);
      }
   }

}
