package homework.arch.cartservice.rest;

import homework.arch.cartservice.mapper.Mapper;
import homework.arch.cartservice.persistence.CartEntity;
import homework.arch.cartservice.persistence.CartRepository;
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
public class CartApiImpl {
    private final CartRepository cartRepository;
    private final Mapper mapper;
}
