package com.linkedin.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

/**
 * JWT Authentication filter
 *
 * Applied to all routes except /api/v1/auth/** --it contains the register and login
 * Flow:
 * 1. Extract JWT from Authorization header
 * 2. Validate the JWT Signature and expiry
 * 3. Extract userId from token claims
 * 4. We will add that userId to request header for downstream services
 * 5. Forward request to correct services
 *
 * If JWT missing is invalid or missing -> 401 Unauthorized
 */
@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

   @Value("${jwt.secret-key}")
   private String secretKey;

   public JwtAuthFilter() {
      super(Config.class);
   }

   @Override
   public GatewayFilter apply(Config config){
      return (exchange, chain) -> {
         String authHeader = exchange.getRequest()
                 .getHeaders()
                 .getFirst(HttpHeaders.AUTHORIZATION);


         if(authHeader == null || !authHeader.startsWith("Bearer ")){ //7 comes from this bearer
            log.warn("Missing or Invalid JWT Token");
            return unauthorized(exchange);
         }

         String token = authHeader.substring(7);
         // Validate the JWT token here

         try{

            Claims claims = extractClaims(token);
            String userId = claims.get("userId", String.class);
            String email = claims.get("email", String.class);

            log.info("User {} has been successfully authenticated", userId);

            //add userId to request header for downstream services

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(r-> r.header("X-User-Id",  userId))
                    .build();

            return chain.filter(modifiedExchange);

         } catch (Exception e) {
            log.error("JWT Validation failed: {}", e.getMessage());
            return unauthorized(exchange);
         }
      };
   }

   private Claims  extractClaims(String token) {
      return Jwts.parser()
              .setSigningKey(getSignKey())
              .build()
              .parseClaimsJws(token)
              .getBody();
   }

   private Key getSignKey() {
      byte[] keyBytes = Decoders.BASE64.decode(secretKey);
      return Keys.hmacShaKeyFor(keyBytes);
   }

   private Mono<Void> unauthorized(ServerWebExchange exchange){
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
   }

   public static class Config{
   //for filter factory
   }















}
