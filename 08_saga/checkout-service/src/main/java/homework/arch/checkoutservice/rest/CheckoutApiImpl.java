package homework.arch.checkoutservice.rest;

import homework.arch.checkoutservice.exception.NotFoundException;
import homework.arch.checkoutservice.exception.NotSufficientFundsException;
import homework.arch.checkoutservice.mapper.Mapper;
import homework.arch.checkoutservice.persistence.AccountReplenishmentRepository;
import homework.arch.checkoutservice.persistence.AccountRepository;
import homework.arch.checkoutservice.persistence.OrderPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CheckoutApiImpl {
    private final AccountRepository accountRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final AccountReplenishmentRepository accountReplenishmentRepository;
    private final Mapper mapper;
}
