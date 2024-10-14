package homework.arch.stockservice.rest;

import homework.arch.exception.NotFoundException;
import homework.arch.idempotency.Idempotent;
import homework.arch.monitoring.ExecutionMonitoring;
import homework.arch.stockservice.api.dto.generated.GetReservations200Response;
import homework.arch.stockservice.api.dto.generated.OrderHandoverDto;
import homework.arch.stockservice.api.dto.generated.ReserveDto;
import homework.arch.stockservice.api.dto.generated.ReservedProductDto;
import homework.arch.stockservice.api.generated.StockApi;
import homework.arch.stockservice.client.delivery.dto.generated.DeliveryDto;
import homework.arch.stockservice.client.delivery.generated.DeliveryApiClient;
import homework.arch.stockservice.client.order.dto.generated.OrderStatusDto;
import homework.arch.stockservice.client.order.generated.OrderApiClient;
import homework.arch.stockservice.exception.NotSufficientOnStockException;
import homework.arch.stockservice.kafka.AvailableProductProducer;
import homework.arch.stockservice.mapper.Mapper;
import homework.arch.stockservice.persistence.ProductEntity;
import homework.arch.stockservice.persistence.ProductRepository;
import homework.arch.stockservice.persistence.ReserveEntity;
import homework.arch.stockservice.persistence.ReserveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StockApiImpl implements StockApi {
    private final int EVERY_MINUTE_IN_MS = 1000 * 60;
    private final ReserveRepository reserveRepository;
    private final Mapper mapper;
    private final AvailableProductProducer productProducer;
    private final ProductRepository productRepository;
    private final DeliveryApiClient deliveryApiClient;
    private final OrderApiClient orderApiClient;
    @Value("${cartReserveMinutes:30}")
    private int cartReserveMinutes;
    @Value("${toLoadDifferentProducts:100}")
    private int toLoadDifferentProducts;
    @Value("${toLoadProductQuantityOnStock:10000}")
    private int toLoadProductQuantityOnStock;
    private static ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    @Transactional
    @ExecutionMonitoring
    @Idempotent
    public ResponseEntity<Void> reserveProducts(UUID idempotencyKey, ReserveDto reserveDto) {
        if (reserveDto.getCartId() == null) {
            throw new IllegalArgumentException();
        }
        if (reserveDto.getOrderId() == null) {
            reserveRepository.saveAll(reserveDto.getProducts().stream().map(product -> reserveAvailableProduct(reserveDto, product)).toList());
            log.debug("Reserved for cart: {}", reserveDto);
            sendOutAvailableProducts(reserveDto.getProducts().stream().map(ReservedProductDto::getProductId).collect(Collectors.toSet()));
        } else {
            reserveRepository.saveAll(reserveRepository.findAllByCartIdAndOrderIdIsNullAndReservationTimestampGreaterThan(reserveDto.getCartId(), LocalDateTime.now().minusMinutes(cartReserveMinutes)).stream()
                            .peek(p -> p.setOrderId(reserveDto.getOrderId())
                                    .setReservationTimestamp(null)).toList());
            log.debug("Reserved for order: {}", reserveDto);
            emulateOrderDeliveryPreparing(reserveDto.getOrderId());
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @Transactional
    @ExecutionMonitoring
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
    @ExecutionMonitoring
    public ResponseEntity<GetReservations200Response> getReservations(UUID productId) {
        var counts = getReservedCounts(productId);
        var result = new GetReservations200Response();
        result.setInCarts(counts.inCartsByProductId.entrySet().stream().map(e -> new ReservedProductDto().productId(e.getKey()).quantity(e.getValue())).toList());
        result.setOrdered(counts.orderedByProductId.entrySet().stream().map(e -> new ReservedProductDto().productId(e.getKey()).quantity(e.getValue())).toList());
        return ResponseEntity.ok(result);
    }

    @Override
    @Transactional
    @ExecutionMonitoring
    @Idempotent
    public ResponseEntity<Void> orderHandover(UUID idempotencyKey, OrderHandoverDto orderHandoverDto) {
        var reserves = reserveRepository.findAllByOrderId(orderHandoverDto.getOrderId());
        var productIdsToUpdateAvailable = new HashSet<UUID>();
        for (ReserveEntity reserve : reserves) {
            var product = productRepository.findById(reserve.getProductId()).orElseThrow(() -> new NotFoundException(reserve.getProductId().toString()));
            productIdsToUpdateAvailable.add(product.getId());
            product.setOnStock(product.getOnStock() - reserve.getQuantity());
            productRepository.save(product);
        }
        reserveRepository.deleteAll(reserves);
        deliveryApiClient.orderHandover(new DeliveryDto().orderId(orderHandoverDto.getOrderId()));
        sendOutAvailableProducts(productIdsToUpdateAvailable);
        return ResponseEntity.noContent().build();
    }

    private ReservedProductCounts getReservedCounts(UUID productId) {
        var reservations = productId == null ? reserveRepository.findAll() : reserveRepository.findAllByProductId(productId);
        var inCarts = StreamSupport.stream(reservations.spliterator(), false)
                .filter(re -> isNull(re.getOrderId())
                        && re.getReservationTimestamp().isAfter(LocalDateTime.now().minusMinutes(cartReserveMinutes))).toList();
        var ordered = StreamSupport.stream(reservations.spliterator(), false).filter(re -> nonNull(re.getOrderId())).toList();
        return new ReservedProductCounts(countReservations(inCarts), countReservations(ordered));
    }

    private Map<UUID, Integer> countReservations(List<ReserveEntity> list) {
        var inCartsByProductsId = list.stream().collect(Collectors.groupingBy(ReserveEntity::getProductId, Collectors.mapping(ReserveEntity::getQuantity, Collectors.reducing(Integer::sum))));
        return inCartsByProductsId.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(0)));
    }

    // TODO: make it concurrent safe
    private ReserveEntity reserveAvailableProduct(ReserveDto reserveDto, ReservedProductDto toReserveProductDto) {
        var productOnStock = productRepository.findById(toReserveProductDto.getProductId()).orElseThrow(() -> new NotFoundException(toReserveProductDto.getProductId().toString()));
        var reservedCounts = getReservedCounts(toReserveProductDto.getProductId());
        if (productOnStock.getOnStock() - reservedCounts.getReserved(toReserveProductDto.getProductId())
                    > toReserveProductDto.getQuantity()) {
            return new ReserveEntity()
                    .setCartId(reserveDto.getCartId())
                    .setProductId(toReserveProductDto.getProductId())
                    .setQuantity(toReserveProductDto.getQuantity());
        } else {
            throw new NotSufficientOnStockException(toReserveProductDto.getProductId().toString());
        }
    }

    @Override
    public ResponseEntity<Void> loadProducts() {
        for (int i = 0; i < toLoadDifferentProducts; i++) {
            var product = productRepository.save(new ProductEntity().setOnStock(toLoadProductQuantityOnStock));
            productProducer.sendMessage(mapper.toKafkaDto(product));
        }
        return ResponseEntity.ok().build();
    }

    private void sendOutAvailableProducts(Set<UUID> productIds) {
        var products = productRepository.findAllById(productIds);
        for (var product : products) {
            var reservedCounts = getReservedCounts(product.getId());
            productProducer.sendMessage(mapper.toKafkaDto(product).setAvailable(product.getOnStock() - reservedCounts.getReserved(product.getId())));
        }
    }

    @Scheduled(fixedDelay = EVERY_MINUTE_IN_MS)
    @Transactional
    protected void clearOutdatedCartReservations() {
        var outdatedReservations = reserveRepository.findAllByOrderIdIsNullAndReservationTimestampLessThan(LocalDateTime.now().minusMinutes(cartReserveMinutes));
        log.debug("to remove " + outdatedReservations.size() + " outdated cart reservations");
        sendOutAvailableProducts(outdatedReservations.stream().map(ReserveEntity::getProductId).collect(Collectors.toSet()));
        reserveRepository.deleteAllById(outdatedReservations.stream().map(ReserveEntity::getId).toList());
    }

    record ReservedProductCounts(Map<UUID, Integer> inCartsByProductId, Map<UUID, Integer> orderedByProductId) {
        int getReserved(UUID productId) {
            return inCartsByProductId.getOrDefault(productId, 0) + orderedByProductId.getOrDefault(productId, 0);
        }
    }

    private void emulateOrderDeliveryPreparing(UUID orderId) {
        executor.execute(() -> {
            for(int i = 0; i < 300; i++) {
                try {
                    Thread.sleep(1000);
                    var order = orderApiClient.getOrder(orderId).getBody();
                    if (order != null) {
                        if (order.getStatus() == OrderStatusDto.FAILED) {
                            log.info("Order {} failed", orderId);
                            break;
                        } else if (order.getStatus() == OrderStatusDto.CHARGED) {
                            orderApiClient.updateOrderStatus(orderId, OrderStatusDto.READY_FOR_DELIVERY.getValue());
                            log.info("Order {} ready for delivery", orderId);
                            break;
                        }
                    } else {
                        log.warn("Can't get order {} status", orderId);
                    }
                } catch (Exception ex) {
                    log.warn("emulateOrderDeliveryPreparing exception: {}", ex.getMessage());
                }
            }
        });
    }
}
