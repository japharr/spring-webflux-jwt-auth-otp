package io.github.jelilio.jwtauthotp.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class RandomUtil {
  private static final int DEF_COUNT = 20;
  private static final int DEF_OTP_COUNT = 4;
  private static final int KEY_DEF_COUNT = 20;
  private static final int NAME_DEF_COUNT = 20;

  /**
   * Generate a password.
   *
   * @return the generated password
   */
  public String generatePassword() {
    return RandomStringUtils.randomAlphanumeric(DEF_COUNT);
  }

  /**
   * Generate an activation key.
   *
   * @return the generated activation key
   */
  public String generateActivationKey() {
    return RandomStringUtils.randomNumeric(KEY_DEF_COUNT);
  }

  /**
   * Generate a reset key.
   *
   * @return the generated reset key
   */
  public String generateResetKey() {
    return RandomStringUtils.randomNumeric(KEY_DEF_COUNT);
  }

  /**
   * Generate a reset key.
   *
   * @return the generated reset key
   */
  public String generateNumber() {
    return RandomStringUtils.randomNumeric(NAME_DEF_COUNT);
  }

  /**
   * Generate a reset key.
   *
   * @return the generated reset key
   */
  public String generateOtp() {
    return RandomStringUtils.randomNumeric(DEF_OTP_COUNT);
  }

  /**
   * Generate a reset key.
   *
   * @return the generated reset key
   */
  public String generateOtp(int length) {
    return RandomStringUtils.randomNumeric(length);
  }
}
