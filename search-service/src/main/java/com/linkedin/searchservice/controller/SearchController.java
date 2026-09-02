package com.linkedin.searchservice.controller;

import com.linkedin.searchservice.model.PostDocument;
import com.linkedin.searchservice.model.UserDocument;
import com.linkedin.searchservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {

   private final SearchService searchService;

   /**
    * Search people by name, headline or Location
    */
   @GetMapping("/people")
   public ResponseEntity<List<UserDocument>> searchPeople(
           @RequestParam String q
   ){
      return ResponseEntity.ok(searchService.searchPeople(q));
   }

   /**
    * Search people by skill
    */
   @GetMapping("/skills")
   public ResponseEntity<List<UserDocument>> searchBySkill(
           @RequestParam String skills
   ){
      return ResponseEntity.ok(searchService.searchBySkills(skills));
   }

   /**
    * Search post by content
    */
   @GetMapping("/posts")
   public ResponseEntity<List<PostDocument>> searchPost(
           @RequestParam String q
   ){
      return ResponseEntity.ok(searchService.searchPosts(q));
   }


}
