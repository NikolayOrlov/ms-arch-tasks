package homework.arch.orderservice.rest;

import homework.arch.orderservice.api.dto.generated.OrderDto;
import homework.arch.orderservice.api.generated.OrderApi;
import homework.arch.orderservice.client.checkout.dto.generated.OrderPaymentDto;
import homework.arch.orderservice.client.checkout.generated.CheckoutApiClient;
import homework.arch.orderservice.client.notification.dto.generated.NotificationDto;
import homework.arch.orderservice.client.notification.generated.NotificationApiClient;
import homework.arch.orderservice.mapper.Mapper;
import homework.arch.orderservice.persistence.OrderEntity;
import homework.arch.orderservice.persistence.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderApiImpl implements OrderApi {
    private final OrderRepository orderRepository;
    private final Mapper mapper;
    private final CheckoutApiClient checkoutClient;
    private final NotificationApiClient notificationClient;

    @Override
    public ResponseEntity<List<OrderDto>> getOrders(UUID customerId) {
        var result = orderRepository.findAllByCustomerId(customerId).stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<UUID> createOrder(OrderDto orderDto) {
        var order = orderRepository.save(mapper.toDomain(orderDto));
        var succeed = true;
        try {
            checkoutClient.createOrderPayment(new OrderPaymentDto()
                                                .orderId(order.getId())
                                                .customerId(order.getCustomerId())
                                                .amount(order.getPrice()));
            log.debug("Order {} payment success", order.getId());
        } catch (Exception ex) {
            succeed = false;
            log.debug("Order {} payment fail due to {}", order.getId(), ex.getMessage());
        }

        notificationClient.notifyCustomer(new NotificationDto()
                                                .notificationWay(NotificationDto.NotificationWayEnum.EMAIL)
                                                .cusomerId(order.getCustomerId())
                                                .orderId(order.getId())
                                                .message(succeed
                                                            ? "Order %s payment for amount %s succeeded".formatted(order.getId(), order.getPrice())
                                                            : "Order %s payment for amount %s failed".formatted(order.getId(), order.getPrice())));
        order.setStatus(succeed ? OrderEntity.OrderStatus.CHARGED : OrderEntity.OrderStatus.FAILED);
        order.setDate(LocalDateTime.now());
        orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(order.getId());
    }
}
