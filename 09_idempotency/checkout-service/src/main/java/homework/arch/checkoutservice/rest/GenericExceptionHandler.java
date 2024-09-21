package homework.arch.checkoutservice.rest;

import homework.arch.checkoutservice.exception.CheckoutException;
import homework.arch.checkoutservice.exception.NotFoundException;
import homework.arch.checkoutservice.exception.NotSufficientFundsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@Slf4j
public class GenericExceptionHandler {
    @ExceptionHandler(NotSufficientFundsException.class)
    public ResponseEntity<?> handleNotSufficientFundsException(final Exception ex, final WebRequest request) {
        return new ResponseEntity<>("NotSufficientFunds: %s".formatted(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(final Exception ex, final WebRequest request) {
        return new ResponseEntity<>("NotFound: %s".formatted(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CheckoutException.class)
    public ResponseEntity<?> handleCheckoutException(final Exception ex, final WebRequest request) {
        return new ResponseEntity<>("Failed: %s".formatted(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
