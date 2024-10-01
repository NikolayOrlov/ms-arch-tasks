package homework.arch.stockservice.kafka;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class ProductKafkaDto {
    private UUID id;
    private int available;
    private long version;
}
