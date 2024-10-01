package homework.arch.productservice.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ProductEntity {
    @Id
    private UUID id;

    private String name;

    private String description;

    private BigDecimal cost;

    private int remainingStock;

    private long remainingStockVersion = 0;
}
