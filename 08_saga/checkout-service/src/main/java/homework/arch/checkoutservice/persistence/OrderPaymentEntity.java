package homework.arch.checkoutservice.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPaymentEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    private UUID orderId;

    private UUID cartId;

    private BigDecimal amount;

    private PaymentStatus status;

    private LocalDateTime requestedTimestamp;

    private LocalDateTime confirmedTimestamp;

    public enum PaymentStatus {
        PENDING, DONE, FAILED
    }
}
