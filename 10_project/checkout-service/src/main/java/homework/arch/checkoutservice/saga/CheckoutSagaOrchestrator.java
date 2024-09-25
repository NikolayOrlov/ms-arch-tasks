package homework.arch.checkoutservice.saga;

import homework.arch.checkoutservice.api.dto.generated.CheckoutDto;
import homework.arch.checkoutservice.api.dto.generated.PaymentConfirmationDto;
import homework.arch.checkoutservice.client.cart.dto.generated.CartDto;
import homework.arch.checkoutservice.client.cart.dto.generated.CartStatusDto;
import homework.arch.checkoutservice.client.cart.generated.CartApiClient;
import homework.arch.checkoutservice.client.order.dto.generated.OrderDto;
import homework.arch.checkoutservice.client.order.dto.generated.OrderStatusDto;
import homework.arch.checkoutservice.client.order.generated.OrderApiClient;
import homework.arch.checkoutservice.client.stock.dto.generated.ReserveDto;
import homework.arch.checkoutservice.client.stock.generated.StockApiClient;
import homework.arch.checkoutservice.exception.CheckoutException;
import homework.arch.checkoutservice.mapper.Mapper;
import homework.arch.checkoutservice.persistence.CheckoutEntity;
import homework.arch.checkoutservice.persistence.CheckoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckoutSagaOrchestrator {
    private final CartApiClient cartClient;
    private final OrderApiClient orderClient;
    private final StockApiClient stockClient;
    private final CheckoutRepository checkoutRepository;
    private final Mapper mapper;

    public CheckoutEntity startCheckoutProcess(UUID customerId, CheckoutDto checkoutDto) {
        var checkout = newCheckout(customerId, checkoutDto);
        if (startCartOrdering(checkout)) {
            if (getCart(checkout) && createOrder(checkout)) {
                if (reserveProductsForOrder(checkout)) {
                    return checkoutRepository.save(checkout);
                }
                compensateOrderCreation(checkout);
            }
            compensateCartOrderingStart(checkout);
        }
        throw new CheckoutException("Can't checkout cart %s".formatted(checkout.getCartId()));
    }

    public void pivotalConfirmPayment(String paymentId, PaymentConfirmationDto paymentConfirmationDto) {
        var checkout = checkoutRepository.findById(UUID.fromString(paymentId)).get();
        if (checkout.getStatus() == CheckoutEntity.PaymentStatus.PENDING) {
            checkout.setConfirmedTimestamp(LocalDateTime.now());
            if (paymentConfirmationDto.getPaymentStatus() == PaymentConfirmationDto.PaymentStatusEnum.PROCESSED) {
                checkout.setStatus(CheckoutEntity.PaymentStatus.DONE);
                commitOrderCharged(checkout);
                commitCartOrdered(checkout);
            } else {
                checkout.setStatus(CheckoutEntity.PaymentStatus.FAILED);
                compensateProductReserveForOrder(checkout);
                compensateOrderCreation(checkout);
                compensateCartOrderingStart(checkout);
            }
            checkoutRepository.save(checkout);
        }
    }

    protected CheckoutEntity newCheckout(UUID customerId, CheckoutDto checkoutDto) {
        var checkout = new CheckoutEntity();
        checkout.setCustomerId(customerId);
        checkout.setCartId(UUID.fromString(cartClient.getCustomerCartId(customerId).getBody()));
        checkout.setRequestedTimestamp(LocalDateTime.now());
        checkout.setStatus(CheckoutEntity.PaymentStatus.PENDING);
        return checkout;
    }

    protected boolean reserveProductsForOrder(CheckoutEntity checkout) {
        try {
            var reserveDto = new ReserveDto()
                    .cartId(checkout.getCartId())
                    .orderId(checkout.getOrderId())
                    .products(checkout.getCartDto().getLineItems().stream()
                            .map(mapper::toStockServiceDto).toList());
            return stockClient.reserveProducts(UUID.randomUUID(), reserveDto).getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Can't reserve products for order {}", checkout.getOrderId());
            return false;
        }
    }

    protected boolean compensateProductReserveForOrder(CheckoutEntity checkout) {
        try {
            return stockClient.cancelProductReserveForOrder(new ReserveDto().orderId(checkout.getOrderId()))
                    .getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Can't cancel product reservation for order {}", checkout.getOrderId());
            return false;
        }
    }

    protected boolean startCartOrdering(CheckoutEntity checkout) {
        try {
            return cartClient.updateCartStatus(checkout.getCartId(), CartStatusDto.ORDER_PENDING.getValue())
                    .getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Can't start cart ordering for cart {}", checkout.getCartId());
            return false;
        }
    }

    protected boolean compensateCartOrderingStart(CheckoutEntity checkout) {
        try {
            return cartClient.updateCartStatus(checkout.getCartId(), CartStatusDto.FORMING.getValue()).getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Can't compensate cart ordering start for cart {}", checkout.getCartId());
            return false;
        }
    }

    protected boolean getCart(CheckoutEntity checkout) {
        try {
            var cartResponse = cartClient.getCart(checkout.getCartId());
            if (cartResponse.getStatusCode().is2xxSuccessful()) {
                CartDto cartDto = cartResponse.getBody();
                if (cartDto == null || cartDto.getStatus() != CartStatusDto.ORDER_PENDING) {
                    log.warn("Get cart returns unexpected value: {}", cartDto);
                    throw new IllegalStateException();
                }
                checkout.setAmount(cartDto.getPrice());
                checkout.setCartDto(cartDto);
                return true;
            }
        } catch (Exception ex) {
            log.warn("Can't get cart {}", checkout.getCartId());
        }
        return false;
    }

    protected boolean createOrder(CheckoutEntity checkout) {
        try {
            var cartDto = checkout.getCartDto();
            var orderResponse = orderClient.createOrder(UUID.randomUUID(), new OrderDto()
                            .customerId(checkout.getCustomerId())
                            .lineItems(cartDto.getLineItems().stream().map(mapper::toOrderServiceDto).toList())
                            .price(cartDto.getPrice()));
            if (orderResponse.getStatusCode().is2xxSuccessful()) {
                checkout.setOrderId(UUID.fromString(orderResponse.getBody()));
                return true;
            }
        } catch (Exception ex) {
            log.warn("Can't create order for cart {}", checkout.getCartId());
        }
        return false;
    }

    protected boolean compensateOrderCreation(CheckoutEntity checkout) {
        try {
            return orderClient.updateOrderStatus(checkout.getOrderId(), OrderStatusDto.FAILED.getValue())
                    .getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Can't compensate order {} creation", checkout.getOrderId());
            return false;
        }
    }

    protected boolean commitOrderCharged(CheckoutEntity checkout) {
        try {
            // TODO: do in retries
            return orderClient.updateOrderStatus(checkout.getOrderId(), OrderStatusDto.CHARGED.getValue())
                                                .getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Can't commit order {} charging", checkout.getOrderId());
            return false;
        }
    }

    protected boolean commitCartOrdered(CheckoutEntity checkout) {
        try {
            // TODO: do in retries
            return cartClient.updateCartStatus(checkout.getCartId(), CartStatusDto.ORDERED.getValue()).getStatusCode()
                                                .is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Can't commit cart {} ordering", checkout.getCartId());
            return false;
        }
    }
}
