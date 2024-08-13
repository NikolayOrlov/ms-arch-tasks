package homework.arch.checkoutservice.mapper;

import homework.arch.checkoutservice.api.dto.generated.AccountDto;
import homework.arch.checkoutservice.api.dto.generated.AccountReplenishmentDto;
import homework.arch.checkoutservice.api.dto.generated.OrderPaymentDto;
import homework.arch.checkoutservice.persistence.AccountEntity;
import homework.arch.checkoutservice.persistence.AccountReplenishmentEntity;
import homework.arch.checkoutservice.persistence.OrderPaymentEntity;
import org.mapstruct.MappingConstants;

@org.mapstruct.Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface Mapper {
    AccountEntity toDomain(AccountDto dto);

    AccountDto toDto(AccountEntity entity);

    OrderPaymentEntity toDomain(OrderPaymentDto dto);
    AccountReplenishmentEntity toDomain(AccountReplenishmentDto dto);
}
