package homework.arch.orderservice.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class OrderEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    private UUID customerId;

    private BigDecimal price;

    private LocalDateTime date;

    private OrderStatus status;

    // TODO: to add line items

    public enum OrderStatus {
        CHARGE_PENDING, CHARGED, FAILED, READY_FOR_DELIVERY, SENT, DELIVERED
    }
}
