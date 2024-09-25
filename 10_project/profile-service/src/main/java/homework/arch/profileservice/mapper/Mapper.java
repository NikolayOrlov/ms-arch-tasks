package homework.arch.profileservice.mapper;

import homework.arch.profileservice.api.dto.generated.CustomerDto;
import homework.arch.profileservice.api.dto.generated.CustomerProfileDto;
import homework.arch.profileservice.persistence.UserDataEntity;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@org.mapstruct.Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface Mapper {
    UserDataEntity toEntity(CustomerDto dto);

    CustomerProfileDto toDto(UserDataEntity entity);

    void updateEntity(@MappingTarget UserDataEntity entity, CustomerProfileDto dto);
}
