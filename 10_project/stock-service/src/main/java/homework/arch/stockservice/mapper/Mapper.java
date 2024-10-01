package homework.arch.stockservice.mapper;

import homework.arch.stockservice.kafka.ProductKafkaDto;
import homework.arch.stockservice.persistence.ProductEntity;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@org.mapstruct.Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface Mapper {
    @Mapping(target = "available", source = "onStock")
    @Mapping(target = "version", source = "entity", qualifiedByName = "version")
    ProductKafkaDto toKafkaDto(ProductEntity entity);

    @Named("version")
    default long version(ProductEntity entity) {
        return System.currentTimeMillis();
    }
}
