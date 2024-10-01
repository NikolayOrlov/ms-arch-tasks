package homework.arch.productservice.kafka;

import homework.arch.productservice.persistence.ProductEntity;
import homework.arch.productservice.persistence.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductConsumer {
    private final ProductRepository productRepository;

    @KafkaListener(topics = "${spring.kafka.topic}")
    @Transactional
    public void consume(ProductKafkaDto productDto) {
        log.debug("Received product message: {}", productDto);
        // TODO: do it atomic and concurrent safe
        var product = productRepository.findById(productDto.getId())
                .orElse(new ProductEntity().setId(productDto.getId()));
        if (product.getRemainingStockVersion() < productDto.getVersion()) {
            if (isNewProduct(product)) {
                product = setProductProperties(product);
            }
            productRepository.save(product.setRemainingStock(productDto.getAvailable()).setRemainingStockVersion(productDto.getVersion()));
            log.debug("Product entity for id = {} is added/updated", product.getId());
        }
    }

    private boolean isNewProduct(ProductEntity product) {
        return product.getRemainingStockVersion() == 0;
    }

    private ProductEntity setProductProperties(ProductEntity product) {
        var faker = new Faker();
        var p = product.setName(faker.commerce().productName())
                        .setDescription(faker.lorem().paragraph(faker.number().numberBetween(1, 3)))
                        .setCost(new BigDecimal(faker.commerce().price()));
        System.out.println("%s : %s : %s".formatted(product.getName(), product.getDescription(), product.getCost()));
        return p;
    }
}
