package io.github.jelilio.jwtauthotp.service;

import io.github.jelilio.jwtauthotp.dto.BasicRegisterDto;
import io.github.jelilio.jwtauthotp.entity.User;
import io.github.jelilio.jwtauthotp.model.AuthResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


public interface UserService {

  Mono<User> findByUsername(String username);

  Mono<Boolean> checkIfEmailAvailable(String email);

  Mono<Pair<User, Long>> authenticate(String usernameOrEmail, String password);

  Mono<AuthResponse> authenticateOtp(String usernameOrEmail, String otpKey);

  @Transactional
  Mono<Pair<User, Long>> register(BasicRegisterDto request);

  Mono<AuthResponse> verifyEmail(String email, String otpKey);

  Mono<Pair<User, Long>> requestOtp(String email);
}
