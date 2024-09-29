package homework.arch.productservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductConsumer {
    @KafkaListener(topics = "${spring.kafka.topic}")
    public void consume(ProductKafkaDto productDto) {
        log.debug("Received product message: {}", productDto);
    }
}
