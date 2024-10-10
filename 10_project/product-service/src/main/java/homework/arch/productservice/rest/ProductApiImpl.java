package homework.arch.productservice.rest;

import homework.arch.exception.NotFoundException;
import homework.arch.productservice.api.dto.generated.ProductDto;
import homework.arch.productservice.api.dto.generated.ProductQueryFilterDto;
import homework.arch.productservice.api.generated.ProductApi ;
import homework.arch.productservice.mapper.Mapper;
import homework.arch.productservice.persistence.ProductEntity;
import homework.arch.productservice.persistence.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static org.springframework.util.StringUtils.hasText;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductApiImpl implements ProductApi {
    private final ProductRepository productRepository;
    private final Mapper mapper;

    @Override
    public ResponseEntity<List<ProductDto>> searchProducts(ProductQueryFilterDto productQueryFilterDto) {
        log.debug("searchProducts: {}", productQueryFilterDto);
        return ResponseEntity.ok(productRepository.findAll().stream().filter(getSearchPredicate(productQueryFilterDto)).map(mapper::toDto).toList());
    }

    private Predicate<ProductEntity> getSearchPredicate(ProductQueryFilterDto productQueryFilterDto) {
        return p -> (!hasText(productQueryFilterDto.getTextQuery()) || p.getName().contains(productQueryFilterDto.getTextQuery()) || p.getDescription().contains(productQueryFilterDto.getTextQuery()))
                && (productQueryFilterDto.getMinCost() == null || p.getCost().compareTo(productQueryFilterDto.getMinCost()) > 0)
                && (productQueryFilterDto.getMaxCost() == null || p.getCost().compareTo(productQueryFilterDto.getMaxCost()) < 0);
    }

    @Override
    public ResponseEntity<ProductDto> getProduct(UUID id) {
        log.debug("getProduct: {}", id);
        return ResponseEntity.ok(mapper.toDto(productRepository.findById(id).orElseThrow(() -> new NotFoundException(id.toString()))));
    }
}
