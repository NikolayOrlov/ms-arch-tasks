package homework.arch.checkoutservice.rest;

import homework.arch.checkoutservice.api.dto.generated.Checkout200Response;
import homework.arch.checkoutservice.api.dto.generated.CheckoutDto;
import homework.arch.checkoutservice.api.dto.generated.PaymentConfirmationDto;
import homework.arch.checkoutservice.api.dto.generated.PaymentWayDto;
import homework.arch.checkoutservice.api.generated.CheckoutApi;
import homework.arch.checkoutservice.mapper.Mapper;
import homework.arch.checkoutservice.persistence.CheckoutEntity;
import homework.arch.checkoutservice.saga.CheckoutSagaOrchestrator;
import homework.arch.idempotency.Idempotent;
import homework.arch.monitoring.ExecutionMonitoring;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
@Slf4j
public class CheckoutApiImpl implements CheckoutApi {
    private final CheckoutSagaOrchestrator checkoutSagaOrchestrator;
    private final Mapper mapper;
    @Value("${paymentSystemUrl:}")
    private String paymentSystemUrl;

    @Override
    @ExecutionMonitoring
    @Idempotent
    public ResponseEntity<Checkout200Response> checkout(UUID idempotencyKey, CheckoutDto checkoutDto) {
        var payment = checkoutSagaOrchestrator.startCheckoutProcess(checkoutDto);
        return ResponseEntity.ok(new Checkout200Response()
                .payment(mapper.toDto(payment)
                                    .paymentWay(new PaymentWayDto().type(PaymentWayDto.TypeEnum.CARD)))
                .paymentSystemRef(getPaymentSystemRef(payment)));
    }

    @Override
    @ExecutionMonitoring
    public ResponseEntity<Void> confirmPayment(String paymentId, PaymentConfirmationDto paymentConfirmationDto) {
        checkoutSagaOrchestrator.pivotalConfirmPayment(paymentId, paymentConfirmationDto);
        return ResponseEntity.noContent().build();
    }

    private String getPaymentSystemRef(CheckoutEntity payment) {
        return paymentSystemUrl;
    }
}
