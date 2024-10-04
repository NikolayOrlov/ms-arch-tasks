package homework.arch.deliveryservice.mapper;

import homework.arch.deliveryservice.api.dto.generated.DeliveryDto;
import homework.arch.deliveryservice.persistence.DeliveryEntity;
import org.mapstruct.MappingConstants;

@org.mapstruct.Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface Mapper {
    DeliveryDto toDto(DeliveryEntity entity);
}
