package homework.arch.checkoutservice.rest;

import homework.arch.checkoutservice.api.dto.generated.AccountDto;
import homework.arch.checkoutservice.api.dto.generated.AccountReplenishmentDto;
import homework.arch.checkoutservice.api.dto.generated.OrderPaymentDto;
import homework.arch.checkoutservice.api.generated.CheckoutApi;
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
public class CheckoutApiImpl implements CheckoutApi {
    private final AccountRepository accountRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final AccountReplenishmentRepository accountReplenishmentRepository;
    private final Mapper mapper;

    @Override
    public ResponseEntity<Void> createAccount(AccountDto accountDto) {
        log.debug("{}", accountDto);
        accountRepository.save(mapper.toDomain(accountDto));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<AccountDto> getAccount(UUID customerId) {
        log.debug("{}", customerId);
        var accountDto = accountRepository.findByCustomerId(customerId)
                            .map(mapper::toDto)
                            .orElseThrow(() -> new NotFoundException(customerId.toString()));
        return ResponseEntity.ok(accountDto);
    }

    @Override
    public ResponseEntity<UUID> createOrderPayment(OrderPaymentDto orderPaymentDto) {
        log.debug("{}", orderPaymentDto);
        var account = accountRepository.findByCustomerId(orderPaymentDto.getCustomerId())
                            .orElseThrow(() -> new NotFoundException(orderPaymentDto.getCustomerId().toString()));
        if (account.getBalance().compareTo(orderPaymentDto.getAmount()) > 0) {
            var payment = mapper.toDomain(orderPaymentDto);
            payment.setAccountId(account.getId());
            orderPaymentRepository.save(payment);
        } else {
            throw new NotSufficientFundsException("Недостаточно средств");
        }
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<UUID> replenishAccount(AccountReplenishmentDto accountReplenishmentDto) {
        log.debug("{}", accountReplenishmentDto);
        var account = accountRepository.findByCustomerId(accountReplenishmentDto.getCustomerId())
                .orElseThrow(() -> new NotFoundException(accountReplenishmentDto.getCustomerId().toString()));
        var replenishment = mapper.toDomain(accountReplenishmentDto);
        replenishment.setAccountId(account.getId());
        accountReplenishmentRepository.save(replenishment);
        return ResponseEntity.ok().build();
    }
}
