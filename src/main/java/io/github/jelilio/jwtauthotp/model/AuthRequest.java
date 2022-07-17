package io.github.jelilio.jwtauthotp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


public record AuthRequest(
    @NotNull @NotBlank
    String username,
    @NotNull @NotBlank
    String password
) { }
