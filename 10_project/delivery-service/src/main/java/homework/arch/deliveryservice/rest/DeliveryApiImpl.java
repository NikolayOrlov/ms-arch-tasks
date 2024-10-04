package homework.arch.deliveryservice.rest;

import homework.arch.deliveryservice.api.dto.generated.DeliveryDto;
import homework.arch.deliveryservice.mapper.Mapper;
import homework.arch.deliveryservice.persistence.DeliveryEntity;
import homework.arch.deliveryservice.persistence.DeliveryRepository;
import homework.arch.exception.NotFoundException;
import homework.arch.idempotency.Idempotent;
import homework.arch.monitoring.ExecutionMonitoring;
import homework.arch.deliveryservice.api.generated.DeliveryApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DeliveryApiImpl implements DeliveryApi {
    private final DeliveryRepository deliveryRepository;
    private final Mapper mapper;

    @Override
    @Idempotent
    @ExecutionMonitoring
    public ResponseEntity<Void> newDelivery(UUID idempotencyKey, DeliveryDto deliveryDto) {
        deliveryRepository.save(new DeliveryEntity());
        // TODO: interaction with an external delivery provider service to request the order delivery
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<DeliveryDto> getDelivery(UUID orderId) {
        return ResponseEntity.ok(mapper.toDto(deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException(orderId.toString()))));
    }

    @Override
    @ExecutionMonitoring
    @Transactional
    public ResponseEntity<Void> orderHandover(DeliveryDto deliveryDto) {
        var delivery = deliveryRepository.findByOrderId(deliveryDto.getOrderId())
                .orElseThrow(() -> new NotFoundException(deliveryDto.getOrderId().toString()));
        delivery.setStatus(DeliveryEntity.DeliveryStatus.SENT);
        deliveryRepository.save(delivery);
        return ResponseEntity.noContent().build();
    }

    @Override
    @ExecutionMonitoring
    @Transactional
    public ResponseEntity<Void> deliveryConfirmed(DeliveryDto deliveryDto) {
        var delivery = deliveryRepository.findByOrderId(deliveryDto.getOrderId())
                .orElseThrow(() -> new NotFoundException(deliveryDto.getOrderId().toString()));
        delivery.setStatus(DeliveryEntity.DeliveryStatus.DELIVERED);
        deliveryRepository.save(delivery);
        return ResponseEntity.noContent().build();
    }
}
