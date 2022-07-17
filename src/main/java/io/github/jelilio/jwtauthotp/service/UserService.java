package io.github.jelilio.jwtauthotp.service;

import io.github.jelilio.jwtauthotp.config.security.PBKDF2Encoder;
import io.github.jelilio.jwtauthotp.entity.User;
import io.github.jelilio.jwtauthotp.exception.AlreadyExistException;
import io.github.jelilio.jwtauthotp.model.AuthRequest;
import io.github.jelilio.jwtauthotp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PBKDF2Encoder passwordEncoder;

    public UserService(UserRepository userRepository, PBKDF2Encoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    public Mono<Boolean> checkIfUsernameExist(String username) {
        return userRepository.countByUsernameIgnoreCase(username)
            .map(count -> count > 0);
    }

    @Transactional
    public Mono<User> register(AuthRequest request) {
        return checkIfUsernameExist(request.getUsername())
            .flatMap(itExist -> {
                if(itExist) return Mono.error(new AlreadyExistException("username already exist"));

                User user = new User();
                user.setUsername(request.getUsername());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                return userRepository.save(user);
            });

    }
}
