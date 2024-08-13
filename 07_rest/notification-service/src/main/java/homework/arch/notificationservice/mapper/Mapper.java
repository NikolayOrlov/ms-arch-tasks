package homework.arch.notificationservice.mapper;

import homework.arch.notificationservice.api.dto.generated.NotificationDto;
import homework.arch.notificationservice.persistence.NotificationEntity;
import org.mapstruct.MappingConstants;

@org.mapstruct.Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface Mapper {
    NotificationEntity toDomain(NotificationDto dto);
    NotificationDto toDto(NotificationEntity entity);
}
