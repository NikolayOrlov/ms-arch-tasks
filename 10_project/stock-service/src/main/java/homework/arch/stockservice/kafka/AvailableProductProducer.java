package homework.arch.stockservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AvailableProductProducer {
    @Value("${spring.kafka.topic}")
    private String topic;

    private final KafkaTemplate<String, ProductKafkaDto> kafkaTemplate;

    public void sendMessage(ProductKafkaDto product) {
        this.kafkaTemplate.send(this.topic, product.getId().toString(), product);
        log.debug("Available product -> {}", product);
    }

}
