package homework.arch.checkoutservice.saga;

import homework.arch.checkoutservice.api.dto.generated.CheckoutDto;
import homework.arch.checkoutservice.api.dto.generated.PaymentConfirmationDto;
import homework.arch.checkoutservice.client.cart.dto.generated.LineItemDto;
import homework.arch.checkoutservice.client.cart.generated.CartApiClient;
import homework.arch.checkoutservice.client.order.dto.generated.OrderDto;
import homework.arch.checkoutservice.client.order.generated.OrderApiClient;
import homework.arch.checkoutservice.client.stock.dto.generated.ReserveDto;
import homework.arch.checkoutservice.client.stock.generated.StockApiClient;
import homework.arch.checkoutservice.exception.CheckoutException;
import homework.arch.checkoutservice.mapper.Mapper;
import homework.arch.checkoutservice.persistence.OrderPaymentEntity;
import homework.arch.checkoutservice.persistence.OrderPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static homework.arch.checkoutservice.client.cart.dto.generated.CartStatusDto.*;
import static homework.arch.checkoutservice.client.order.dto.generated.OrderStatusDto.CHARGED;
import static homework.arch.checkoutservice.client.order.dto.generated.OrderStatusDto.FAILED;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckoutSagaOrchestrator {
    private final CartApiClient cartClient;
    private final OrderApiClient orderClient;
    private final StockApiClient stockClient;
    private final OrderPaymentRepository orderPaymentRepository;
    private final Mapper mapper;

    public OrderPaymentEntity checkout(CheckoutDto checkoutDto) {
        var customerId = checkoutDto.getCustomerId();
        var checkoutContext = newCheckoutContext(customerId);
        if (startCartOrdering(checkoutContext)) {
            if (updateCheckoutContextWithCart(checkoutContext)) {
                var orderId = orderClient.createOrder(new OrderDto()
                                .lineItems(checkoutContext.getCartDto().getLineItems().stream().map(mapper::toOrderServiceDto).toList())
                                .price(checkoutContext.getCartDto().getPrice()))
                        .getBody();
                stockClient.reserveProducts(getReserveDto(checkoutContext.getCartId(), orderId, checkoutContext.getCartDto().getLineItems()));
                // TODO: cart, order, reservation rollback updates in reverse order on any failure
                return orderPaymentRepository.save(OrderPaymentEntity.builder()
                        .orderId(orderId)
                        .cartId(checkoutContext.getCartId())
                        .amount(checkoutContext.getCartDto().getPrice())
                        .status(OrderPaymentEntity.PaymentStatus.PENDING)
                        .requestedTimestamp(LocalDateTime.now())
                        .build());
            }
        }
        throw new CheckoutException("Can't checkout cart %s".formatted(checkoutContext.getCartId()));
    }

    protected CheckoutContext newCheckoutContext(UUID customerId) {
        var ctx = new CheckoutContext();
        ctx.setCartId(cartClient.getCustomerCart(customerId).getBody());
        return ctx;
    }

    protected boolean startCartOrdering(CheckoutContext checkoutContext) {
        try {
            return cartClient.updateCartStatus(checkoutContext.getCartId(), ORDER_PENDING.getValue())
                    .getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("{}", checkoutContext.getCartId());
            return false;
        }
    }

    protected boolean updateCheckoutContextWithCart(CheckoutContext checkoutContext) {
        try {
            var cartResponse = cartClient.getCart(checkoutContext.getCartId());
            if (cartResponse.getStatusCode().is2xxSuccessful()) {
                checkoutContext.setCartDto(cartResponse.getBody());
                return true;
            }
            // TODO: retries on retriable error statuses
        } catch (Exception ex) {
            log.warn("{}", checkoutContext.getCartId());
        }
        return false;
    }

    public void confirmPayment(String paymentId, PaymentConfirmationDto paymentConfirmationDto) {
        var payment = orderPaymentRepository.findById(UUID.fromString(paymentId)).get();
        payment.setConfirmedTimestamp(LocalDateTime.now());
        if (paymentConfirmationDto.getPaymentStatus() == PaymentConfirmationDto.PaymentStatusEnum.PROCESSED) {
            payment.setStatus(OrderPaymentEntity.PaymentStatus.DONE);
            // cart, order commits:
            cartClient.updateCartStatus(payment.getCartId(), ORDERED.getValue());
            orderClient.updateOrderStatus(payment.getOrderId(), CHARGED.getValue());
        } else {
            payment.setStatus(OrderPaymentEntity.PaymentStatus.FAILED);
            // cart, order, reservation rollback updates in reverse order:
            stockClient.cancelProductReserveForOrder(new ReserveDto().orderId(payment.getOrderId()));
            orderClient.updateOrderStatus(payment.getOrderId(), FAILED.getValue());
            cartClient.updateCartStatus(payment.getCartId(), ORDER_FAILED.getValue());
        }
        orderPaymentRepository.save(payment);
    }

    protected ReserveDto getReserveDto(UUID cartId, UUID orderId, List<LineItemDto> lineItems) {
        return new ReserveDto()
                .cartId(cartId)
                .orderId(orderId)
                .products(lineItems.stream().map(mapper::toStockServiceDto).toList());

    }


    // TODO: implement API client calls with retries on retriable / logging on non retriable errors
}
