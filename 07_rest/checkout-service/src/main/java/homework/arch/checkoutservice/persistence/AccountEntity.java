package homework.arch.checkoutservice.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;

    @Column(unique=true)
    private UUID customerId;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountId", referencedColumnName = "id")
    private List<OrderPaymentEntity> orderPayments = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountId", referencedColumnName = "id")
    private List<AccountReplenishmentEntity> accountReplenishments = new ArrayList<>();

    public BigDecimal getBalance() {
        return getSum(getAccountReplenishments().stream().map(AccountReplenishmentEntity::getAmount))
                .subtract(getSum(getOrderPayments().stream().map(OrderPaymentEntity::getAmount)));
    }

    private BigDecimal getSum(Stream<BigDecimal> amounts) {
        return amounts.reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }
}
