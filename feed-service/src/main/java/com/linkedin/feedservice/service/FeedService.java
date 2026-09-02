package com.linkedin.feedservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedService {

   private final RedisTemplate<String, String> redisTemplate;

   private static final String FEED_KEY_PREFIX = "feed";

   public List<String> getFeed(String userId, int page, int size) {

      log.info("Getting feed for userId: {}", userId);

      String feedKey = FEED_KEY_PREFIX + userId;

      //pagination Post Range
      int start = page * size;
      int end = start + size - 1;

      List<String> postIds = redisTemplate.opsForList().range(feedKey, start, end);

      if (postIds.isEmpty()) {
         log.info("No feed found for userId: {}", userId);
         return new ArrayList<>();
      }

      List<String> result = postIds.stream()
              .map(Object::toString)
              .toList();

      log.info("Returning {} posts for user: {}", result.size(), userId);

      return result;
   }

   /**
    * Clearing feed for the consumer
    * @param userId
    */
   public void clearFeed(String userId) {
      String  feedKey = FEED_KEY_PREFIX + userId;
      redisTemplate.delete(feedKey);
      log.info("Deleted feed cache for userId: {}", userId);
   }
}