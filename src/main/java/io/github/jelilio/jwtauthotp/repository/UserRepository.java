package io.github.jelilio.jwtauthotp.repository;

import io.github.jelilio.jwtauthotp.entity.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
  Mono<User> findByUsernameIgnoreCase(String username);
  Mono<Long> countByUsernameIgnoreCase(String username);
}