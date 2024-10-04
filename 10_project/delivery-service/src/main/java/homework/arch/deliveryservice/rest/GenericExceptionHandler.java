package homework.arch.deliveryservice.rest;

import homework.arch.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@Slf4j
public class GenericExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(final Exception ex, final WebRequest request) {
        return new ResponseEntity<>("NotFound: %s".formatted(ex.getMessage()), HttpStatus.NOT_FOUND);
    }
}
