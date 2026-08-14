package school.hei.haapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<String> handleNotFound(NotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<String> handleConflict(ConflictException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
  }
}
