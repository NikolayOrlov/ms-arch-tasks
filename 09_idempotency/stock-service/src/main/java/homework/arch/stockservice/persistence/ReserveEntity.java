package homework.arch.stockservice.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ReserveEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    private UUID cartId;

    private UUID orderId;

    private UUID productId;

    private LocalDateTime reservationTimestamp = LocalDateTime.now();

    private int quantity;
}
