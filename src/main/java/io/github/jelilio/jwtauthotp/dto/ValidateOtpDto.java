package io.github.jelilio.jwtauthotp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record ValidateOtpDto(
    @NotNull @NotBlank
    String email,
    @NotNull @NotBlank
    String otpKey
) {
}
