package io.github.jelilio.jwtauthotp.config.security;


import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

public abstract class SecurityUtil {
  public static Mono<String> loggedInUsername() {
    return ReactiveSecurityContextHolder.getContext()
        .map(context -> context.getAuthentication().getPrincipal())
        .cast(String.class);
  }
}
