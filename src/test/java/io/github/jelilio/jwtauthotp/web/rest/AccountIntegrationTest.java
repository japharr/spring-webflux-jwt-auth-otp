package io.github.jelilio.jwtauthotp.web.rest;

import io.github.jelilio.jwtauthotp.dto.BasicRegisterDto;
import io.github.jelilio.jwtauthotp.dto.ValidateOtpDto;
import io.github.jelilio.jwtauthotp.model.AuthResponse;
import io.github.jelilio.jwtauthotp.model.OtpResponseDto;
import io.github.jelilio.jwtauthotp.util.RandomUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountIntegrationTest {
  @Autowired
  WebTestClient webClient;

  @MockBean
  RandomUtil randomUtil;

  static {
    GenericContainer<?> redis =
        new GenericContainer<>(DockerImageName.parse("redis:5.0.3-alpine")).withExposedPorts(6379);
      redis.start();
      System.setProperty("spring.redis.host", redis.getHost());
      System.setProperty("spring.redis.port", redis.getMappedPort(6379).toString());
  }

  @BeforeEach
  public void setUp() {}

  @Test
  public void test() {
    Assertions.assertThat(1).isEqualTo(1);
  }

  @Test
  @Order(1)
  public void can_register() {
    var register = new BasicRegisterDto("John", "john@mail.com", "password");

    when(randomUtil.generateOtp()).thenReturn("1234");

    webClient
        .post().uri("/api/account/register")
        .bodyValue(register)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(OtpResponseDto.class);
  }

  @Test
  @Order(2)
  public void can_validate_email() {
    var dto = new ValidateOtpDto("john@mail.com", "1234");

    webClient
        .post().uri("/api/account/verify-email-otp")
        .bodyValue(dto)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(AuthResponse.class);
  }
}
