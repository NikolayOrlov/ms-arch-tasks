package homework.arch.authservice.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceGateway {
    private final RestClient profileServiceRestClient;

    public void sendUserData(UserDataDao userData) {
        profileServiceRestClient.post()
                .uri("/private-api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .body(userData)
                .retrieve()
                .toBodilessEntity();
    }
}
