package io.github.jelilio.jwtauthotp.service.impl;

import io.github.jelilio.jwtauthotp.config.security.PBKDF2Encoder;
import io.github.jelilio.jwtauthotp.dto.BasicRegisterDto;
import io.github.jelilio.jwtauthotp.entity.User;
import io.github.jelilio.jwtauthotp.entity.enumeration.Role;
import io.github.jelilio.jwtauthotp.exception.AlreadyExistException;
import io.github.jelilio.jwtauthotp.repository.UserRepository;
import io.github.jelilio.jwtauthotp.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PBKDF2Encoder passwordEncoder;

  public UserServiceImpl(UserRepository userRepository, PBKDF2Encoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Mono<User> findByUsername(String username) {
    return userRepository.findByUsernameIgnoreCase(username);
  }

  private Mono<Boolean> checkIfUsernameExist(String username) {
    return userRepository.countByUsernameIgnoreCase(username)
        .map(count -> count > 0);
  }

  @Override
  @Transactional
  public Mono<User> register(BasicRegisterDto request) {
    return checkIfUsernameExist(request.email())
        .flatMap(itExist -> {
          if(itExist) return Mono.error(new AlreadyExistException("username already exist"));

          User user = new User();
          user.setName(request.name());
          user.setUsername(request.email());
          user.setEmail(request.email());
          user.setPassword(passwordEncoder.encode(request.password()));
          user.setRoles(Set.of(Role.ROLE_USER));
          return userRepository.save(user);
        });

  }
}
