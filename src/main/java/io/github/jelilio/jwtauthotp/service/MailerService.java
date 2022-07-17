package io.github.jelilio.jwtauthotp.service;

import io.github.jelilio.jwtauthotp.entity.User;
import reactor.core.publisher.Mono;

public interface MailerService {
  Mono<String> sendOtpMail(User user, String otpKey, Long otpKeyDuration);

  Mono<String> sendActivationMail(User user);
}
