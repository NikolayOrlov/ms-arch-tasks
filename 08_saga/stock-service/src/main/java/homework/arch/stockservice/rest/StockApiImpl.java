package homework.arch.stockservice.rest;

import homework.arch.stockservice.mapper.Mapper;
import homework.arch.stockservice.persistence.ReserveEntity;
import homework.arch.stockservice.persistence.ReserveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StockApiImpl {
    private final ReserveRepository reserveRepository;
    private final Mapper mapper;
}
