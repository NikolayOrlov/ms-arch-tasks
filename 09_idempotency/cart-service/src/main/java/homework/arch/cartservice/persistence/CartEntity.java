package homework.arch.cartservice.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CartEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    private UUID customerId;

    private CartStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cartId")
    private List<LineItemEntity> lineItems = new ArrayList<>();

    public enum CartStatus {
        FORMING, ORDER_PENDING, ORDERED, ORDER_FAILED
    }
}
