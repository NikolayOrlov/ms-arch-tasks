package homework.arch.stockservice.rest;

import homework.arch.idempotency.Idempotent;
import homework.arch.monitoring.ExecutionMonitoring;
import homework.arch.stockservice.api.dto.generated.GetReservations200Response;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.util.CollectionUtils.isEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
    @ExecutionMonitoring
    @Idempotent
    public ResponseEntity<Void> reserveProducts(UUID idempotencyKey, ReserveDto reserveDto) {
        if (reserveDto.getCartId() == null) {
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
            reserveRepository.saveAll(reserveRepository.findAllByCartIdAndOrderIdIsNullAndReservationTimestampGreaterThan(reserveDto.getCartId(), LocalDateTime.now().minusMinutes(cartReserveMinutes)).stream()
                            .peek(p -> p.setOrderId(reserveDto.getOrderId())
                                    .setReservationTimestamp(null)).toList());
            log.debug("Reserved for order: {}", reserveDto);
        }
        return ResponseEntity.noContent().build();
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

    @Override
    public ResponseEntity<GetReservations200Response> getReservations(UUID productId) {
        var reservations = productId == null ? reserveRepository.findAll() : reserveRepository.findAllByProductId(productId);
        var inCarts = StreamSupport.stream(reservations.spliterator(), false)
                        .filter(re -> isNull(re.getOrderId())
                                && re.getReservationTimestamp().isAfter(LocalDateTime.now().minusMinutes(cartReserveMinutes))).toList();
        var ordered = StreamSupport.stream(reservations.spliterator(), false).filter(re -> nonNull(re.getOrderId())).toList();
        var inCartsByProductId = count(inCarts);
        var orderedByProductId = count(ordered);
        var result = new GetReservations200Response();
        result.setInCarts(inCartsByProductId.entrySet().stream().map(e -> new ReservedProductDto().productId(e.getKey()).quantity(e.getValue())).toList());
        result.setOrdered(orderedByProductId.entrySet().stream().map(e -> new ReservedProductDto().productId(e.getKey()).quantity(e.getValue())).toList());
        return ResponseEntity.ok(result);
    }

    private Map<UUID, Integer> count(List<ReserveEntity> list) {
        var inCartsByProductsId = list.stream().collect(Collectors.groupingBy(ReserveEntity::getProductId, Collectors.mapping(ReserveEntity::getQuantity, Collectors.reducing(Integer::sum))));
        return inCartsByProductsId.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(0)));
    }
}
