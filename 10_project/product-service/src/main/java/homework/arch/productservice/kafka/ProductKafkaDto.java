package homework.arch.productservice.kafka;

import lombok.Data;

import java.util.UUID;

@Data
public class ProductKafkaDto {
    private UUID id;
    private int available;
    private long version;
}
