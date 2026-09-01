package com.linkedin.userservice.service;

import com.linkedin.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

   private final UserRepository userRepository;


}
