package io.github.jelilio.jwtauthotp.repository;

import io.github.jelilio.jwtauthotp.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, UUID> {
  Mono<User> findByUsernameIgnoreCase(String username);
  Mono<Long> countByUsernameIgnoreCase(String username);
  @Query("select count(*) from users where email = :email and activated_date != null")
  Mono<Long> countByEmailAvailable(String email);

  @Query("select * from users where email = :email and activated_date = null limit 1")
  Mono<User> findByEmailAndNotActivated(String email);
}
