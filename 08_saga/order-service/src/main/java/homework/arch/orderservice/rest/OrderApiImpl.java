package homework.arch.orderservice.rest;

import homework.arch.orderservice.api.dto.generated.OrderDto;
import homework.arch.orderservice.api.generated.OrderApi;
import homework.arch.orderservice.exception.NotFoundException;
import homework.arch.orderservice.mapper.Mapper;
import homework.arch.orderservice.persistence.OrderEntity;
import homework.arch.orderservice.persistence.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderApiImpl implements OrderApi {
    private final OrderRepository orderRepository;
    private final Mapper mapper;

    @Override
    public ResponseEntity<String> createOrder(OrderDto orderDto) {
        var order = orderRepository.save(mapper.toDomain(orderDto).setStatus(OrderEntity.OrderStatus.CHARGE_PENDING));
        log.debug("Created order {}", order.getId());
        return ResponseEntity.ok(order.getId().toString());
    }

    @Override
    @Transactional
    public ResponseEntity<Void> updateOrderStatus(UUID orderId, String newStatusAsString) {
        var newStatus = OrderEntity.OrderStatus.valueOf(newStatusAsString);
        var order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException(orderId.toString()));
        order.setStatus(newStatus);
        orderRepository.save(order);
        log.debug("Order {} set to status '{}'", orderId, newStatusAsString);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<OrderDto>> getCustomerOrders(UUID customerId) {
        var result = orderRepository.findAllByCustomerId(customerId).stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<OrderDto> getOrder(UUID orderId) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException(orderId.toString()));
        return ResponseEntity.ok(mapper.toDto(order));
    }
}
