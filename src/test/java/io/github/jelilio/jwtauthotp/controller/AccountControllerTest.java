package io.github.jelilio.jwtauthotp.controller;

import io.github.jelilio.jwtauthotp.config.security.JWTUtil;
import io.github.jelilio.jwtauthotp.config.security.PBKDF2Encoder;
import io.github.jelilio.jwtauthotp.dto.BasicRegisterDto;
import io.github.jelilio.jwtauthotp.dto.ValidateOtpDto;
import io.github.jelilio.jwtauthotp.entity.User;
import io.github.jelilio.jwtauthotp.model.AuthResponse;
import io.github.jelilio.jwtauthotp.model.OtpResponseDto;
import io.github.jelilio.jwtauthotp.repository.UserRepository;
import io.github.jelilio.jwtauthotp.service.MailerService;
import io.github.jelilio.jwtauthotp.service.UserService;
import io.github.jelilio.jwtauthotp.service.impl.UserServiceImpl;
import io.github.jelilio.jwtauthotp.util.RandomUtil;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

//@SpringBootTest
//@Import({UserServiceImpl.class, PBKDF2Encoder.class, RandomUtil.class, JWTUtil.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerTest {
  @Autowired
  private WebTestClient webClient;

  @MockBean
  private UserService userService;

  @MockBean
  private RandomUtil randomUtil;

  @Test
  public void can_register() {
    var register = new BasicRegisterDto("John", "john@mail.com", "password");

    var user = new User(register.name(), register.email(), register.password());

    when(userService.register(register)).thenReturn(Mono.just(Pair.of(user, 300L)));

    webClient
        .post().uri("/api/account/register")
        .bodyValue(register)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(OtpResponseDto.class)
        .isEqualTo(new OtpResponseDto(300L));
  }

  @Test
  public void can_validate_email() {
    var dto = new ValidateOtpDto("john@mail.com", "1234");

    when(userService.verifyEmail("john@mail.com", "1234")).thenReturn(Mono.just(new AuthResponse("token1234")));

    webClient
        .post().uri("/api/account/verify-email-otp")
        .bodyValue(dto)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(AuthResponse.class)
        .isEqualTo(new AuthResponse("token1234"));
  }
}
