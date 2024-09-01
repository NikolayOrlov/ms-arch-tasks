package homework.arch.cartservice.rest;

import homework.arch.cartservice.api.dto.generated.CartDto;
import homework.arch.cartservice.api.dto.generated.LineItemDto;
import homework.arch.cartservice.api.generated.CartApi;
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
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CartApiImpl implements CartApi {
    private final CartRepository cartRepository;
    private final Mapper mapper;

    @Override
    @Transactional
    public ResponseEntity<Void> newCustomerCartLineItem(LineItemDto lineItemDto) {
        var customerId = lineItemDto.getCustomerId(); // TODO: to be replaced by data from auth header
        var cart = cartRepository.findByCustomerIdAndStatus(customerId, CartEntity.CartStatus.FORMING)
                .orElse(new CartEntity().setCustomerId(customerId).setStatus(CartEntity.CartStatus.FORMING));
        cart.getLineItems().add(mapper.toEntity(lineItemDto)); // TODO: price is supposed to come from internal API
        cartRepository.save(cart);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<LineItemDto>> getCustomerCartLineItems(UUID customerId) {
        var cart = cartRepository.findByCustomerIdAndStatus(customerId, CartEntity.CartStatus.FORMING)
                .orElseThrow(() -> new NotFoundException(customerId.toString()));
        return ResponseEntity.ok(cart.getLineItems().stream().map(mapper::toDto).toList());
    }

    @Override
    public ResponseEntity<CartDto> getCart(UUID cartId) {
        return ResponseEntity.ok(mapper.toDto(cartRepository.findById(cartId)
                                    .orElseThrow(() -> new NotFoundException(cartId.toString()))));
    }

    @Override
    public ResponseEntity<String> getCustomerCart(UUID customerId) {
        return ResponseEntity.ok(cartRepository.findByCustomerIdAndStatus(customerId, CartEntity.CartStatus.FORMING)
                    .map(CartEntity::getId).map(UUID::toString)
                    .orElseThrow(NotFoundException::new));
    }

    @Override
    @Transactional
    public ResponseEntity<Void> updateCartStatus(UUID cartId, String body) {
        var newStatus = CartEntity.CartStatus.valueOf(body);
        var cart = cartRepository.findById(cartId).orElseThrow(() -> new NotFoundException(cartId.toString()));
        if (newStatus == CartEntity.CartStatus.ORDER_PENDING && cart.getStatus() != CartEntity.CartStatus.FORMING
            || newStatus == CartEntity.CartStatus.ORDERED && cart.getStatus() != CartEntity.CartStatus.ORDER_PENDING) {
            throw new CartException(cart.getId().toString());
        }
        cart.setStatus(newStatus);
        cartRepository.save(cart);
        return ResponseEntity.noContent().build();
    }


}
