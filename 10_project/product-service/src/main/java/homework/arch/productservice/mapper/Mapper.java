package homework.arch.productservice.mapper;

import homework.arch.productservice.api.dto.generated.ProductDto;
import homework.arch.productservice.persistence.ProductEntity;
import org.mapstruct.MappingConstants;

@org.mapstruct.Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface Mapper {
    ProductEntity toDomain(ProductDto dto);
    ProductDto toDto(ProductEntity entity);
}
