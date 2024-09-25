package homework.arch.orderservice.rest;

import homework.arch.exception.NotFoundException;
import homework.arch.exception.UnauthorizedException;
import homework.arch.idempotency.Idempotent;
import homework.arch.monitoring.ExecutionMonitoring;
import homework.arch.orderservice.api.dto.generated.OrderDto;
import homework.arch.orderservice.api.generated.OrderApi;
import homework.arch.orderservice.mapper.Mapper;
import homework.arch.orderservice.persistence.OrderEntity;
import homework.arch.orderservice.persistence.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static homework.arch.token.TokenExtractor.getAuthenticatedUserId;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderApiImpl implements OrderApi {
    private final OrderRepository orderRepository;
    private final Mapper mapper;
    private final HttpServletRequest httpServletRequest;

    @Override
    @ExecutionMonitoring
    @Idempotent
    public ResponseEntity<String> createOrder(UUID idempotencyKey, OrderDto orderDto) {
        var order = orderRepository.save(mapper.toDomain(orderDto).setStatus(OrderEntity.OrderStatus.CHARGE_PENDING));
        log.debug("Created order {}", order.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(order.getId().toString());
    }

    @Override
    @Transactional
    @ExecutionMonitoring
    public ResponseEntity<Void> updateOrderStatus(UUID orderId, String newStatusAsString) {
        var newStatus = OrderEntity.OrderStatus.valueOf(newStatusAsString);
        var order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException(orderId.toString()));
        order.setStatus(newStatus);
        orderRepository.save(order);
        log.debug("Order {} set to status '{}'", orderId, newStatusAsString);
        return ResponseEntity.noContent().build();
    }

    @Override
    @ExecutionMonitoring
    public ResponseEntity<List<OrderDto>> getCustomerOrders() {
        var customerId = getAuthenticatedUserId(httpServletRequest);
        var result = orderRepository.findAllByCustomerId(customerId).stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(result);
    }

    @Override
    @ExecutionMonitoring
    public ResponseEntity<OrderDto> getOrder(UUID orderId) {
        var customerId = getAuthenticatedUserId(httpServletRequest);
        var order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException(orderId.toString()));
        if (!order.getCustomerId().equals(customerId)) {
            throw new UnauthorizedException(customerId.toString());
        }
        return ResponseEntity.ok(mapper.toDto(order));
    }
}
