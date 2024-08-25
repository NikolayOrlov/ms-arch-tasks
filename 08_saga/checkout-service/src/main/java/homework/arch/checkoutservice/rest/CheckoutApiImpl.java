package homework.arch.checkoutservice.rest;

import homework.arch.checkoutservice.api.dto.generated.Checkout200Response;
import homework.arch.checkoutservice.api.dto.generated.CheckoutDto;
import homework.arch.checkoutservice.api.dto.generated.PaymentConfirmationDto;
import homework.arch.checkoutservice.api.dto.generated.PaymentWayDto;
import homework.arch.checkoutservice.api.generated.CheckoutApi;
import homework.arch.checkoutservice.client.cart.generated.CartApiClient;
import homework.arch.checkoutservice.client.cart.dto.generated.LineItemDto;
import homework.arch.checkoutservice.client.order.dto.generated.OrderDto;
import homework.arch.checkoutservice.client.order.generated.OrderApiClient;
import homework.arch.checkoutservice.client.stock.dto.generated.ReserveDto;
import homework.arch.checkoutservice.client.stock.generated.StockApiClient;
import homework.arch.checkoutservice.mapper.Mapper;
import homework.arch.checkoutservice.persistence.OrderPaymentEntity;
import homework.arch.checkoutservice.persistence.OrderPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static homework.arch.checkoutservice.client.cart.dto.generated.CartStatusDto.ORDER_PENDING;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CheckoutApiImpl implements CheckoutApi {
    private final CartApiClient cartClient;
    private final OrderApiClient orderClient;
    private final StockApiClient stockClient;
    private final OrderPaymentRepository orderPaymentRepository;
    private final Mapper mapper;
    @Value("paymentSystemUrl:")
    private String paymentSystemUrl;

    @Override
    public ResponseEntity<Checkout200Response> checkout(CheckoutDto checkoutDto) {
        var customerId = checkoutDto.getCustomerId();
        var cartId = cartClient.getCustomerCart(customerId).getBody();
        cartClient.updateCartStatus(cartId, ORDER_PENDING.getValue());
        var cart = cartClient.getCart(cartId).getBody();
        var orderId = orderClient.createOrder(new OrderDto()
                                                .lineItems(cart.getLineItems().stream().map(mapper::toOrderServiceDto).toList())
                                                .price(cart.getPrice()))
                        .getBody();
        stockClient.reserveProducts(getReserveDto(cartId, orderId, cart.getLineItems()));
        // TODO: cart, order, reservation rollback updates in reverse order on any failure
        var payment = orderPaymentRepository.save(OrderPaymentEntity.builder()
                                                                        .orderId(orderId)
                                                                        .cartId(cartId)
                                                                        .amount(cart.getPrice())
                                                                        .status(OrderPaymentEntity.PaymentStatus.PENDING)
                                                                        .requestedTimestamp(LocalDateTime.now())
                                                                        .build());
        return ResponseEntity.ok(new Checkout200Response()
                .payment(mapper.toDto(payment)
                                    .paymentWay(new PaymentWayDto().type(PaymentWayDto.TypeEnum.CARD)))
                .paymentSystemRef(getPaymentSystemRef(payment)));
    }

    @Override
    public ResponseEntity<Void> confirmPayment(String id, PaymentConfirmationDto paymentConfirmationDto) {
        var payment = orderPaymentRepository.findById(UUID.fromString(id)).get();
        payment.setConfirmedTimestamp(LocalDateTime.now());
        if (paymentConfirmationDto.getPaymentStatus() == PaymentConfirmationDto.PaymentStatusEnum.PROCESSED) {
            payment.setStatus(OrderPaymentEntity.PaymentStatus.DONE);
            // TODO: cart, order, reservation commits
        } else {
            payment.setStatus(OrderPaymentEntity.PaymentStatus.FAILED);
            // TODO: cart, order, reservation rollback updates in reverse order
        }
        orderPaymentRepository.save(payment);
        return ResponseEntity.noContent().build();
    }

    private ReserveDto getReserveDto(UUID cartId, UUID orderId, List<LineItemDto> lineItems) {
        return new ReserveDto()
                .cartId(cartId)
                .orderId(orderId)
                .products(lineItems.stream().map(mapper::toStockServiceDto).toList());

    }

    private String getPaymentSystemRef(OrderPaymentEntity payment) {
        return paymentSystemUrl;
    }
}
