package io.github.jelilio.jwtauthotp.config.security;


import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class SecurityUtil {
  public static Mono<String> loggedInUsername() {
    return ReactiveSecurityContextHolder.getContext()
        .map(context -> context.getAuthentication().getPrincipal())
        .cast(String.class);
  }

  public static Mono<Claims> loggedInCredentials() {
    return ReactiveSecurityContextHolder.getContext()
        .map(context -> context.getAuthentication().getCredentials())
        .cast(Claims.class);
  }

  public static Mono<String> loggedInUserId() {
    return loggedInCredentials()
        .map(claims -> (String) claims.getOrDefault(JWTUtil.USER_ID, null));
  }
}
