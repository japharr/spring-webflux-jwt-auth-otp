package io.github.jelilio.jwtauthotp.service.impl;

import io.github.jelilio.jwtauthotp.config.security.JWTUtil;
import io.github.jelilio.jwtauthotp.config.security.PBKDF2Encoder;
import io.github.jelilio.jwtauthotp.dto.BasicRegisterDto;
import io.github.jelilio.jwtauthotp.entity.User;
import io.github.jelilio.jwtauthotp.entity.enumeration.Role;
import io.github.jelilio.jwtauthotp.exception.AlreadyExistException;
import io.github.jelilio.jwtauthotp.exception.AuthenticationException;
import io.github.jelilio.jwtauthotp.model.AuthResponse;
import io.github.jelilio.jwtauthotp.repository.UserRepository;
import io.github.jelilio.jwtauthotp.service.MailerService;
import io.github.jelilio.jwtauthotp.service.UserService;
import io.github.jelilio.jwtauthotp.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static io.github.jelilio.jwtauthotp.exception.AuthenticationException.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PBKDF2Encoder passwordEncoder;
  private final RandomUtil randomUtil;
  private final MailerService mailerService;

  private final JWTUtil jwtUtil;
  private final ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate;

  @Value("${jwt-auth-otp.otp.duration}")
  private Long otpKeyDuration; // in seconds

  @Override
  public Mono<User> findByUsername(String username) {
    return userRepository.findByUsernameIgnoreCase(username);
  }

  private Mono<Boolean> checkIfUsernameExist(String username) {
    return userRepository.countByUsernameIgnoreCase(username)
        .map(count -> count > 0);
  }

  @Override
  public Mono<Boolean> checkIfEmailAvailable(String email) {
    return userRepository.countByEmailAvailable(email)
        .map(count -> {
          log.info("countByEmailAvailable: {}", count);
          return count == 0;
        });
  }

  @Override
  public Mono<Pair<User, Long>> authenticate(String usernameOrEmail, String password) {
    log.debug("usernameOrEmail: {}, password: {}", usernameOrEmail, password);

    var invalidAuthentication = new AuthenticationException("Invalid username or password", AUTH_LOGIN_INVALID);

    Mono<User> userUni = userRepository.findByUsernameOrEmail(usernameOrEmail)
        .switchIfEmpty(Mono.error(() -> invalidAuthentication));

    return userUni.flatMap(user -> {
      if(!user.isEnabled()) {
        return Mono.error(() -> new AuthenticationException("Your account has been disabled, contact your administrator", AUTH_LOGIN_DISABLED));
      }

      if(user.getEmail().equalsIgnoreCase(usernameOrEmail) && !user.isActivated()) {
        return Mono.error(() -> new AuthenticationException("Please, verify your email address", AUTH_VERIFY_EMAIL));
      }

      if(user.getPassword().equals(passwordEncoder.encode(password))) {
        return createOtp(usernameOrEmail, user);
      }

      return Mono.error(() -> invalidAuthentication);
    });
  }

  @Override
  @Transactional
  public Mono<AuthResponse> authenticateOtp(String usernameOrEmail, String otpKey) {
    return validateOtp(usernameOrEmail, otpKey, false).flatMap(user -> {
      user.setLastLoginDate(Instant.now());
      return userRepository.save(user);
    }).flatMap(this::createToken);
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
      log.info("register: emailAvailable: {}", tuple2.getT1());

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

  @Override
  public Mono<AuthResponse> verifyEmail(String email, String otpKey) {
    return validateOtp(email, otpKey, true).flatMap(user -> {
      log.info("verifyEmail: user validated");
      user.setActivatedDate(Instant.now());
      user.setLastLoginDate(Instant.now());
      return userRepository.save(user);
    }).flatMap(user -> mailerService.sendActivationMail(user)
        .flatMap(__ -> createToken(user)));
  }

  @Override
  public Mono<Pair<User, Long>> requestOtp(String usernameOrEmail) {
    Mono<User> loginUni = userRepository.findByUsernameOrEmail(usernameOrEmail)
        .switchIfEmpty(Mono.error(() -> new AuthenticationException("Email or username not registered", AUTH_LOGIN_INVALID)));

    return loginUni.flatMap(user -> {
      if (!user.isEnabled()) {
        return Mono.error(() -> new AuthenticationException("Your account has been disabled, contact your administrator", AUTH_LOGIN_DISABLED));
      }

      if(user.isActivated()) {
        return Mono.error(() -> new AuthenticationException("Already activated", AUTH_LOGIN_ACTIVATED));
      }

      return createOtp(usernameOrEmail, user);
    });
  }

  private Mono<User> validateOtp(String usernameOrEmail, String otpKey, boolean register) {
    Mono<User> userUni = userRepository.findByUsernameOrEmail(usernameOrEmail)
        .switchIfEmpty(Mono.error(() -> new AuthenticationException("No user with this email/username found", AUTH_LOGIN_INVALID)));

    return userUni.flatMap(user -> {
      if(!user.isEnabled()) {
        return Mono.error(() -> new AuthenticationException("Your account has been disabled, contact your administrator", AUTH_LOGIN_DISABLED));
      }

      if(user.isActivated() && register) {
        return Mono.error(() -> new AuthenticationException("Already activated", AUTH_LOGIN_ACTIVATED));
      }

      return reactiveStringRedisTemplate.opsForValue().get(usernameOrEmail).flatMap(response -> {
        if(response == null) {
          return Mono.error(() -> new AuthenticationException("Expired OTP", AUTH_OTP_EXPIRED));
        }

        if(!response.equalsIgnoreCase(otpKey)) {
          return Mono.error(() -> new AuthenticationException("Invalid OTP", AUTH_OTP_INVALID));
        }

        return Mono.just(user);
      });
    });
  }

  private Mono<AuthResponse> createToken(User user) {
    try {
      Mono<AuthResponse> response = Mono.just(new AuthResponse(jwtUtil.generateToken(user)));

      user.setLastLoginDate(Instant.now());
      return userRepository.save(user).flatMap(__ -> response);
    } catch (Exception e) {
      return Mono.error(() -> new AuthenticationException("Unable to generate token, try again later", AUTH_BAD_TOKEN));
    }
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
