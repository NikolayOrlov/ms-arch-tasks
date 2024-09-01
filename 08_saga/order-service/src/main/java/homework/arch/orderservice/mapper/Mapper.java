package homework.arch.orderservice.mapper;

import homework.arch.orderservice.api.dto.generated.OrderDto;
import homework.arch.orderservice.persistence.OrderEntity;
import org.mapstruct.MappingConstants;

@org.mapstruct.Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface Mapper {
    OrderEntity toDomain(OrderDto dto);
}
