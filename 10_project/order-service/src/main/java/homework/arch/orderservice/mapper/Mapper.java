package homework.arch.orderservice.mapper;

import homework.arch.orderservice.api.dto.generated.OrderDto;
import homework.arch.orderservice.client.notification.dto.generated.NotificationDto;
import homework.arch.orderservice.persistence.NotificationEntity;
import homework.arch.orderservice.persistence.OrderEntity;
import org.mapstruct.MappingConstants;

@org.mapstruct.Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface Mapper {
    OrderEntity toDomain(OrderDto dto);
    OrderDto toDto(OrderEntity entity);
    NotificationDto toDto(NotificationEntity entity);
    NotificationEntity toDomain(NotificationDto dto);
}
