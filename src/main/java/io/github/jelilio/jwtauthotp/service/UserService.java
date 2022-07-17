package io.github.jelilio.jwtauthotp.service;

import io.github.jelilio.jwtauthotp.dto.BasicRegisterDto;
import io.github.jelilio.jwtauthotp.entity.User;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


public interface UserService {

  Mono<User> findByUsername(String username);

  @Transactional
  Mono<User> register(BasicRegisterDto request);
}
