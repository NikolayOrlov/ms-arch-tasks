package homework.arch.checkoutservice.saga;

import homework.arch.checkoutservice.client.cart.dto.generated.CartDto;
import lombok.Data;

import java.util.UUID;

@Data
public class CheckoutContext {
    private UUID cartId;
    private CartDto cartDto;
    private UUID orderId;
}
