package homework.arch.orderservice.mapper;

import homework.arch.orderservice.api.dto.generated.OrderDto;
import homework.arch.orderservice.client.notification.dto.generated.NotificationDto;
import homework.arch.orderservice.client.delivery.dto.generated.DeliveryStatusDto;
import homework.arch.orderservice.persistence.NotificationEntity;
import homework.arch.orderservice.persistence.OrderEntity;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;

@org.mapstruct.Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface Mapper {
    @Mapping(target = "date", ignore = true)
    OrderEntity toDomain(OrderDto dto);

    OrderDto toDto(OrderEntity entity);

    NotificationDto toDto(NotificationEntity entity);

    NotificationEntity toDomain(NotificationDto dto);

    @ValueMapping(target = "READY_FOR_DELIVERY", source = "NEW")
    OrderEntity.OrderStatus toEntity(DeliveryStatusDto dto);
}
