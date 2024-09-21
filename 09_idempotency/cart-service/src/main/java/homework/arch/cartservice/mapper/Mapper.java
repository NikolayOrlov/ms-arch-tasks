package homework.arch.cartservice.mapper;

import homework.arch.cartservice.api.dto.generated.CartDto;
import homework.arch.cartservice.api.dto.generated.LineItemDto;
import homework.arch.cartservice.persistence.CartEntity;
import homework.arch.cartservice.persistence.LineItemEntity;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

@org.mapstruct.Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface Mapper {
    @Mapping(target = "price", source = "lineItems", qualifiedByName = "getCartPrice")
    CartDto toDto(CartEntity entity);

    @Named("getCartPrice")
    default BigDecimal getCartPrice(List<LineItemEntity> lineItems) {
        return lineItems.stream()
                .map(li -> li.getPrice().multiply(BigDecimal.valueOf(li.getQuantity())))
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    LineItemDto toDto(LineItemEntity entity);

    LineItemEntity toDomain(LineItemDto dto);

    homework.arch.cartservice.client.stock.dto.generated.ReservedProductDto toStockServiceDto(
            LineItemDto dto);
}
