package ai.xenopilot.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiErrorResponse> illegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.of(exception.getMessage(), 400));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiErrorResponse> badCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiErrorResponse.of("Invalid email or password", 401));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> notFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorResponse.of(exception.getMessage(), 404));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiErrorResponse> authentication(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiErrorResponse.of(exception.getMessage(), 401));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Validation failed");
        return ResponseEntity.badRequest().body(ApiErrorResponse.of(message, 400));
    }
}
