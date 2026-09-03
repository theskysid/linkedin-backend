package com.linkedin.postservice.service;

import com.linkedin.postservice.entity.Comment;
import com.linkedin.postservice.entity.Like;
import com.linkedin.postservice.entity.Post;
import com.linkedin.postservice.repository.CommentRepository;
import com.linkedin.postservice.repository.LikeRepository;
import com.linkedin.postservice.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

   private final PostRepository postRepository;

   private final LikeRepository likeRepository;

   private final CommentRepository commentRepository;

   private final S3Service s3Service;

   private final KafkaTemplate<String, Post> kafkaTemplate;

   private static final String POST_CREATED_TOPIC = "post.created";

   private static final String POST_LIKED_TOPIC = "post.liked";

   private static final String POST_COMMENTED_TOPIC = "post.commented";

   /**
    * Creates a new post.
    *
    * @param authorId The ID of the user who created the post.
    * @param content  The content of the post.
    * @param image    The image associated with the post (optional).
    *                 optionally upload the image to S3
    *                 publish the post.created to kafka
    *                 feed service and search service will consume this
    * @return The created post.
    */
   public Post createPost(String authorId, String content, MultipartFile image) {

      log.info("Creating post with authorId: {}", authorId);

      Post post = new Post();
      post.setAuthorId(authorId);
      post.setContent(content);

      if(image != null &&  !image.isEmpty()) {
         String imageUrl = s3Service.uploadFile(image, "posts/" + authorId);
         post.setImageUrl(imageUrl);
      }


      Post savedPost =  postRepository.save(post);

      log.info("Created Post : {}", savedPost.getId());

      //publish to kafka -- feed service and search service consume this

      Map<String, Object> postCreatedEvent = new HashMap<>();
      postCreatedEvent.put("postId", savedPost.getId());
      postCreatedEvent.put("authorId", savedPost.getAuthorId());
      postCreatedEvent.put("content", savedPost.getContent());
      postCreatedEvent.put("imageUrl", savedPost.getImageUrl());
      postCreatedEvent.put("createdAt", savedPost.getCreatedAt().toString());

      kafkaTemplate.send(POST_CREATED_TOPIC, savedPost.getId(), (Post) postCreatedEvent);

      log.info("Created post : {}", savedPost.getId());

      return savedPost;

   }
   //get post
   public Post getPost(String postId) {
      return postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found : "+ postId));
   }

   //get user post
   public List<Post> getUserPost(String userId) {
      return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
   }

   //like or unlike a post --important
   public String likePost(String postId, String userId) {
      Post post = getPost(postId);

      if(likeRepository.existsByPostIdAndUserId(postId, userId)) {
         //unlike
         likeRepository.findByPostIdAndUserId(postId, userId)
                 .ifPresent(likeRepository::delete);
         post.setLikeCount(post.getLikeCount() - 1);
         postRepository.save(post);
         return "Post Unliked";
      }

      Like like = Like.builder()
              .postId(postId)
              .userId(userId)
              .build();

      likeRepository.save(like);
      post.setLikeCount(post.getLikeCount() + 1);
      postRepository.save(post);

      //publish post.liked event

      Map<String, Object> postLikedEvent = new HashMap<>();
      postLikedEvent.put("postId", post.getId());
      postLikedEvent.put("userId", userId);
      postLikedEvent.put("authorId", post.getAuthorId());

      kafkaTemplate.send(POST_LIKED_TOPIC, postId, (Post) postLikedEvent);

      return "Post Liked";
   }

   //add comment to post
   public Comment addComment(String postId, String authorId, String content) {
      Post post = getPost(postId);

      Comment comment = Comment.builder()
              .postId(postId)
              .authorId(authorId)
              .content(content)
              .build();

      Comment savedComment = commentRepository.save(comment);

      post.setCommentCount(post.getCommentCount() + 1);
      postRepository.save(post);

      //post post.event

      log.info("Added comment to post : {}", post.getId());

      Map<String, Object> postCommentedEvent = new HashMap<>();
      postCommentedEvent.put("postId",postId);
      postCommentedEvent.put("authorId", authorId); //commenting
      postCommentedEvent.put("commentId", savedComment.getId());
      postCommentedEvent.put("postAuthorId", post.getAuthorId()); //on whose post

      kafkaTemplate.send(POST_COMMENTED_TOPIC, postId, (Post) postCommentedEvent);

      return savedComment;
   }

   //get all commment for a post
   public List<Comment> getComments(String postId) {
      return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
   }

   //delete post method
   public void deletePost(String postId, String userId) {
      Post post = getPost(postId);
      if(!post.getAuthorId().equals(userId)) {
         throw new RuntimeException("Post Author Id not match (Not Authorized to delete this post)");
      }

      postRepository.delete(post);

      log.info("Deleted post : {}", postId);

   }

}
