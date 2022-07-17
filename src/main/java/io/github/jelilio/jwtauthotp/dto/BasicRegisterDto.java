package io.github.jelilio.jwtauthotp.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record BasicRegisterDto(
    @NotNull @NotBlank
    String name,
    @NotNull @NotBlank @Email
    String email,

    @NotNull @NotBlank
    String password
) {
}
