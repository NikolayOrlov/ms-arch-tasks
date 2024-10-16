package homework.arch.orderservice.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class NotificationEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    private UUID customerId;

    private UUID orderId;

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationWay notificationWay;

    @Enumerated(EnumType.STRING)
    private SendStatus sendStatus = SendStatus.TO_BE_SEND;

    public enum NotificationWay {
        EMAIL
    }

    public enum SendStatus {
        TO_BE_SEND, SENT
    }
}
