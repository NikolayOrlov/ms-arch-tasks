package homework.arch.stockservice.rest;

import homework.arch.stockservice.api.dto.generated.ReserveDto;
import homework.arch.stockservice.api.dto.generated.ReservedProductDto;
import homework.arch.stockservice.api.generated.StockApi;
import homework.arch.stockservice.exception.NotFoundException;
import homework.arch.stockservice.mapper.Mapper;
import homework.arch.stockservice.persistence.ReserveEntity;
import homework.arch.stockservice.persistence.ReserveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;
import static org.springframework.util.CollectionUtils.isEmpty;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StockApiImpl implements StockApi {
    private final ReserveRepository reserveRepository;
    private final Mapper mapper;
    @Value("${cartReserveMinutes:30}")
    private int cartReserveMinutes;

    @Override
    @Transactional
    public ResponseEntity<Void> reserveProducts(ReserveDto reserveDto) {
        if (reserveDto.getCartId() == null || isEmpty(reserveDto.getProducts())) {
            throw new IllegalArgumentException();
        }
        if (reserveDto.getOrderId() == null) {
            // TODO: to check remaining stock
            reserveRepository.saveAll(reserveDto.getProducts().stream()
                                                                .map(p -> new ReserveEntity()
                                                                                .setCartId(reserveDto.getCartId())
                                                                                .setProductId(p.getProductId())
                                                                                .setQuantity(p.getQuantity())).toList());
            log.debug("Reserved for cart: {}", reserveDto);
        } else {
            reserveRepository.saveAll(reserveDto.getProducts().stream()
                                            .map(p -> getProductReserveOfCart(reserveDto.getCartId(), p)
                                                            .setOrderId(reserveDto.getOrderId())
                                                            .setReservationTimestamp(null))
                                                        .toList());
            log.debug("Reserved for order: {}", reserveDto);
        }
        return ResponseEntity.noContent().build();
    }

    private ReserveEntity getProductReserveOfCart(UUID cartId, ReservedProductDto reservedProductDto) {
        var reserve = reserveRepository.findByProductIdAndCartIdAndOrderIdIsNullAndReservationTimestampGreaterThan(reservedProductDto.getProductId(), cartId, LocalDateTime.now().minusMinutes(cartReserveMinutes))
                                .orElseThrow(() -> new NotFoundException("%s : %s ".formatted(cartId.toString(), reservedProductDto.getProductId())));
        if (reserve.getQuantity() != reservedProductDto.getQuantity()) {
            throw new NotFoundException("%s : %s : %d".formatted(cartId.toString(), reservedProductDto.getProductId(), reservedProductDto.getQuantity()));
        }
        return reserve;
    }

    @Override
    @Transactional
    public ResponseEntity<Void> cancelProductReserveForOrder(ReserveDto reserveDto) {
        var reserveEntities = reserveRepository.findAllByOrderId(reserveDto.getOrderId()).stream().map(r -> r.setOrderId(null).setReservationTimestamp(LocalDateTime.now())).toList();
        if (!reserveEntities.isEmpty()) {
            reserveRepository.saveAll(reserveEntities);
            log.info("Reserve for order {} is cancelled", reserveDto.getOrderId());
        } else {
            log.warn("Reserve for order {} can't be cancelled due to not found reservations", reserveDto.getOrderId());
        }
        return ResponseEntity.noContent().build();
    }
}
