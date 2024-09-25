package homework.arch.stockservice.rest;

import homework.arch.exception.NotFoundException;
import homework.arch.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@Slf4j
public class GenericExceptionHandler {
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorizedException(final Exception ex, final WebRequest request) {
        return new ResponseEntity<>("Unauthorized: %s".formatted(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(final Exception ex, final WebRequest request) {
        return new ResponseEntity<>("NotFound: %s".formatted(ex.getMessage()), HttpStatus.NOT_FOUND);
    }
}
