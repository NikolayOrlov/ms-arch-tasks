package homework.arch.checkoutservice.mapper;

import homework.arch.checkoutservice.api.dto.generated.PaymentDto;
import homework.arch.checkoutservice.persistence.OrderPaymentEntity;
import org.mapstruct.MappingConstants;

@org.mapstruct.Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface Mapper {
    homework.arch.checkoutservice.client.order.dto.generated.LineItemDto toOrderServiceDto(
            homework.arch.checkoutservice.client.cart.dto.generated.LineItemDto dto);
    homework.arch.checkoutservice.client.stock.dto.generated.ReservedProductDto toStockServiceDto(
            homework.arch.checkoutservice.client.cart.dto.generated.LineItemDto dto);

    PaymentDto toDto(OrderPaymentEntity entity);

}
