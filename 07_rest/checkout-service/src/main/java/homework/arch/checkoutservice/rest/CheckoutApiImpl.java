package homework.arch.checkoutservice.rest;

import homework.arch.checkoutservice.api.dto.generated.AccountDto;
import homework.arch.checkoutservice.api.dto.generated.AccountReplenishmentDto;
import homework.arch.checkoutservice.api.dto.generated.OrderPaymentDto;
import homework.arch.checkoutservice.api.generated.CheckoutApi;
import homework.arch.checkoutservice.exception.NotSufficientFundsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CheckoutApiImpl implements CheckoutApi {

    @Override
    public ResponseEntity<Void> createAccount(AccountDto accountDto) {
        log.debug("{}", accountDto);
        return CheckoutApi.super.createAccount(accountDto);
    }

    @Override
    public ResponseEntity<AccountDto> getAccount(UUID customerId) {
        log.debug("{}", customerId);
        return CheckoutApi.super.getAccount(customerId);
    }

    @Override
    public ResponseEntity<UUID> createOrderPayment(OrderPaymentDto orderPaymentDto) {
        log.debug("{}", orderPaymentDto);
        throw new NotSufficientFundsException("FAIL");
    }

    @Override
    public ResponseEntity<UUID> replenishAccount(AccountReplenishmentDto accountReplenishmentDto) {
        log.debug("{}", accountReplenishmentDto);
        return CheckoutApi.super.replenishAccount(accountReplenishmentDto);
    }
}
