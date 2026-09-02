package com.linkedin.searchservice.service;

import com.linkedin.searchservice.model.PostDocument;
import com.linkedin.searchservice.model.UserDocument;
import com.linkedin.searchservice.repository.PostSearchRepository;
import com.linkedin.searchservice.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {
   private final UserSearchRepository userSearchRepository;
   private final PostSearchRepository postSearchRepository;

   public List<UserDocument> searchPeople(String query) {
      log.info("Searching users: {}", query);
      return userSearchRepository.searchUsers(query);
   }

   /**
    * Search User by skills
    */
   public List<UserDocument> searchBySkills(String query) {
      log.info("Searching user by skills: {}", query);
      return userSearchRepository.findBySkillsContaining(query);
   }

   /**
    * Search post by content
    */
   public List<PostDocument> searchPosts(String query) {
      log.info("Searching posts: {}", query);
      return postSearchRepository.searchPosts(query);
   }
}
