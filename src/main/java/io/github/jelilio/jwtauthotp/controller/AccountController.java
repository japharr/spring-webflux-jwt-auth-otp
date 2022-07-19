package io.github.jelilio.jwtauthotp.controller;

import io.github.jelilio.jwtauthotp.config.security.JWTUtil;
import io.github.jelilio.jwtauthotp.config.security.PBKDF2Encoder;
import io.github.jelilio.jwtauthotp.dto.BasicRegisterDto;
import io.github.jelilio.jwtauthotp.dto.ValidateOtpDto;
import io.github.jelilio.jwtauthotp.model.AuthRequest;
import io.github.jelilio.jwtauthotp.model.AuthResponse;
import io.github.jelilio.jwtauthotp.model.OtpResponseDto;
import io.github.jelilio.jwtauthotp.service.UserService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountController {
    private final UserService userService;

    @PostMapping("/authenticate")
    public Mono<ResponseEntity<OtpResponseDto>> authenticate(@Valid @RequestBody AuthRequest dto) {
        return userService.authenticate(dto.username(), dto.password())
            .map(response -> ResponseEntity.ok(new OtpResponseDto(response.getSecond())));
    }

    @PostMapping("/authenticate-otp")
    public Mono<ResponseEntity<AuthResponse>> authenticateOtp(@Valid @RequestBody ValidateOtpDto dto) {
        return userService.authenticateOtp(dto.email(), dto.otpKey())
            .map(ResponseEntity::ok);
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<OtpResponseDto>> register(@Valid @RequestBody BasicRegisterDto ar) {
        return userService.register(ar)
            .map(inserted -> ResponseEntity
                .created(URI.create("/api/account/register/" + inserted.getFirst().getId()))
                .body(new OtpResponseDto(inserted.getSecond())));
    }

    @PostMapping("/verify-email-otp")
    public Mono<ResponseEntity<AuthResponse>> verifyEmail(@Valid @RequestBody ValidateOtpDto dto) {
        return userService.verifyEmail(dto.email(), dto.otpKey()).map(ResponseEntity::ok);
    }

    @PostMapping("/request-otp")
    public Mono<ResponseEntity<OtpResponseDto>> requestOtp(@Valid @RequestBody String email) {
        return userService.requestOtp(email)
            .map(userLongTuple -> ResponseEntity
                .ok(new OtpResponseDto(userLongTuple.getSecond()))
            );
    }

    @GetMapping("/check-email")
    public Mono<ResponseEntity<Boolean>> checkEmail(@NotNull @RequestParam("email") String email) {
        return userService.checkIfEmailAvailable(email)
            .map(ResponseEntity::ok);
    }
}
