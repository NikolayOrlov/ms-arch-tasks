package homework.arch.stockservice.mapper;

import homework.arch.stockservice.kafka.ProductKafkaDto;
import homework.arch.stockservice.persistence.ProductEntity;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@org.mapstruct.Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface Mapper {
    @Mapping(target = "available", source = "onStock")
    ProductKafkaDto toKafkaDto(ProductEntity entity);
}
