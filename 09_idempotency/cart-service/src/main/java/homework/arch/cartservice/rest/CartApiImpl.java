package homework.arch.cartservice.rest;

import homework.arch.monitoring.ExecutionMonitoring;
import homework.arch.idempotency.Idempotent;
import homework.arch.cartservice.api.dto.generated.CartDto;
import homework.arch.cartservice.api.dto.generated.LineItemDto;
import homework.arch.cartservice.api.generated.CartApi;
import homework.arch.cartservice.client.stock.dto.generated.ReserveDto;
import homework.arch.cartservice.client.stock.generated.StockApiClient;
import homework.arch.cartservice.exception.CartException;
import homework.arch.cartservice.exception.NotFoundException;
import homework.arch.cartservice.mapper.Mapper;
import homework.arch.cartservice.persistence.CartEntity;
import homework.arch.cartservice.persistence.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CartApiImpl implements CartApi {
    private final CartRepository cartRepository;
    private final Mapper mapper;
    private final StockApiClient stockClient;

    @Override
    @Transactional
    @ExecutionMonitoring
    @Idempotent
    public ResponseEntity<String> newCustomerCartLineItem(UUID idempotencyKey, LineItemDto lineItemDto) {
        // TODO: prevent creating new forming cart if there is one in status ORDER_PENDING
        var customerId = lineItemDto.getCustomerId(); // TODO: to be replaced by data from auth header
        var cart = cartRepository.findByCustomerIdAndStatus(customerId, CartEntity.CartStatus.FORMING)
                .orElse(new CartEntity().setCustomerId(customerId).setStatus(CartEntity.CartStatus.FORMING));
        if (!Objects.equals(cart.getId(), lineItemDto.getCartId())) {
            throw new CartException("Unexpected cartId. Customer already has a cart in status FORMING with another id");
        }
        cart.getLineItems().add(mapper.toDomain(lineItemDto)); // TODO: price is supposed to come from internal API
        cart =  cartRepository.save(cart);
        reserveProductsForCart(cart.getId(), lineItemDto);
        log.debug("Added line item to cart {} for product {}", cart.getId(), lineItemDto.getProductId());
        return ResponseEntity.ok(cart.getId().toString());
    }

    @Override
    @ExecutionMonitoring
    public ResponseEntity<CartDto> getCustomerCart(UUID customerId) {
        var cart = cartRepository.findByCustomerIdAndStatus(customerId, CartEntity.CartStatus.FORMING)
                .orElseThrow(() -> new NotFoundException(customerId.toString()));
        return ResponseEntity.ok(mapper.toDto(cart));
    }

    @Override
    @ExecutionMonitoring
    public ResponseEntity<CartDto> getCart(UUID cartId) {
        var cart = cartRepository.findById(cartId)
                                    .orElseThrow(() -> new NotFoundException(cartId.toString()));
        log.debug("Get cart data for {}", cartId);
        return ResponseEntity.ok(mapper.toDto(cart));
    }

    @Override
    @ExecutionMonitoring
    public ResponseEntity<String> getCustomerCartId(UUID customerId) {
        var cart = cartRepository.findByCustomerIdAndStatus(customerId, CartEntity.CartStatus.FORMING)
                .orElseThrow(NotFoundException::new);
        return ResponseEntity.ok(cart.getId().toString());
    }

    @Override
    @Transactional
    @ExecutionMonitoring
    public ResponseEntity<Void> updateCartStatus(UUID cartId, String newStatusAsString) {
        var newStatus = CartEntity.CartStatus.valueOf(newStatusAsString);
        var cart = cartRepository.findById(cartId).orElseThrow(() -> new NotFoundException(cartId.toString()));
        if (newStatus == CartEntity.CartStatus.ORDER_PENDING && cart.getStatus() != CartEntity.CartStatus.FORMING
            || newStatus == CartEntity.CartStatus.ORDERED && cart.getStatus() != CartEntity.CartStatus.ORDER_PENDING) {
            throw new CartException("Unexpected status transitions for cart with id %s".formatted(cart.getId().toString()));
        }
        cart.setStatus(newStatus);
        cartRepository.save(cart);
        log.debug("Cart {} set to status '{}'", cartId, newStatusAsString);
        return ResponseEntity.noContent().build();
    }

    protected void reserveProductsForCart(UUID cartId, LineItemDto lineItemDto) {
        try {
            var reserveDto = new ReserveDto()
                                    .cartId(cartId)
                                    .products(List.of(mapper.toStockServiceDto(lineItemDto)));
            stockClient.reserveProducts(UUID.randomUUID(), reserveDto).getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Can't reserve products for cart {}", cartId);
            throw ex;
        }
    }
}
