package io.github.jelilio.jwtauthotp.service.impl;

import io.github.jelilio.jwtauthotp.entity.User;
import io.github.jelilio.jwtauthotp.service.MailerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class MailServiceImpl implements MailerService {
  @Override
  public Mono<String> sendOtpMail(User user, String otpKey, Long otpKeyDuration) {
    log.info("sendOtpMail: mail: {}, key: {}", user.getEmail(), otpKey);
    return Mono.just("mail sent");
  }

  @Override
  public Mono<String> sendActivationMail(User user) {
    log.info("sendActivationMail: mail: {}", user.getEmail());
    return Mono.just("mail sent");
  }
}
