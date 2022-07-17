package io.github.jelilio.jwtauthotp.service.impl;

import io.github.jelilio.jwtauthotp.config.security.PBKDF2Encoder;
import io.github.jelilio.jwtauthotp.dto.BasicRegisterDto;
import io.github.jelilio.jwtauthotp.entity.User;
import io.github.jelilio.jwtauthotp.entity.enumeration.Role;
import io.github.jelilio.jwtauthotp.exception.AlreadyExistException;
import io.github.jelilio.jwtauthotp.repository.UserRepository;
import io.github.jelilio.jwtauthotp.service.MailerService;
import io.github.jelilio.jwtauthotp.service.UserService;
import io.github.jelilio.jwtauthotp.util.RandomUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Arrays;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PBKDF2Encoder passwordEncoder;
  private final RandomUtil randomUtil;

  private final MailerService mailerService;
  private final ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate;

  @Value("${jwt-auth-otp.otp.duration}")
  private Long otpKeyDuration; // in seconds

  public UserServiceImpl(
      UserRepository userRepository,
      PBKDF2Encoder passwordEncoder,
      RandomUtil randomUtil,
      MailerService mailerService,
      ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.randomUtil = randomUtil;
    this.mailerService = mailerService;
    this.reactiveStringRedisTemplate = reactiveStringRedisTemplate;
  }

  @Override
  public Mono<User> findByUsername(String username) {
    return userRepository.findByUsernameIgnoreCase(username);
  }

  private Mono<Boolean> checkIfUsernameExist(String username) {
    return userRepository.countByUsernameIgnoreCase(username)
        .map(count -> count > 0);
  }

  public Mono<Boolean> checkIfEmailAvailable(String email) {
    return userRepository.countByEmailAvailable(email)
        .map(count -> count == 0);
  }

  @Transactional
  public Mono<User> register2(BasicRegisterDto request) {
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

  @Override
  @Transactional
  public Mono<Pair<User, Long>> register(BasicRegisterDto dto) {
    Mono<Boolean> uniEmailAvailable = checkIfEmailAvailable(dto.email());
    Mono<User> extLoginUni = userRepository.findByEmailAndNotActivated(dto.email())
        .defaultIfEmpty(new User());

    return Mono.zip(uniEmailAvailable, extLoginUni).flatMap(tuple2 -> {
      var emailAvailable = tuple2.getT1();

      if(!emailAvailable) {
        return Mono.error(() -> new AlreadyExistException("Email already in used"));
      }

      final User user = tuple2.getT2();
      user.setName(dto.name());
      user.setEmail(dto.email());
      user.setUsername(dto.email());
      user.setPassword(passwordEncoder.encode(dto.password()));
      user.setRoles(Set.of(Role.ROLE_USER));
      return userRepository.save(user)
          .flatMap(updated -> createOtp(dto.email(), updated));
    });
  }

  @Transactional
  private Mono<Pair<User, Long>> createOtp(String usernameOrEmail, User user) {
    var otpKey = randomUtil.generateOtp();

    return reactiveStringRedisTemplate.opsForValue()
        .set(usernameOrEmail, otpKey, Duration.ofSeconds(otpKeyDuration))
        .flatMap(__ -> mailerService.sendOtpMail(user, otpKey, otpKeyDuration)
            .map(it -> Pair.of(user, otpKeyDuration)));
  }
}
