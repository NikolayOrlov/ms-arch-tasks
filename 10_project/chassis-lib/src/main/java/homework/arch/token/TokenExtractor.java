package homework.arch.token;

import homework.arch.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public abstract class TokenExtractor {
    public static UUID getAuthenticatedUserId(HttpServletRequest request) {
        var id = UUID.fromString(Optional.ofNullable(request.getHeader("X-UserId"))
                .orElseThrow(UnauthorizedException::new));
        log.debug("Authenticated userId = {}", id);
        return id;
    }
}
