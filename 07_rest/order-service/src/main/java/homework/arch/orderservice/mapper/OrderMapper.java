package homework.arch.orderservice.mapper;

import homework.arch.orderservice.api.dto.generated.OrderDto;
import homework.arch.orderservice.persistence.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    OrderEntity toDomain(OrderDto dto);
    OrderDto toDto(OrderEntity entity);
}
