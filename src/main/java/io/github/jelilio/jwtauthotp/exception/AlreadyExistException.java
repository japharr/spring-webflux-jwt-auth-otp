package io.github.jelilio.jwtauthotp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AlreadyExistException extends Exception {
  public AlreadyExistException(String message) {
    super(message);
  }
}
