package homework.arch.authservice.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestClientFactory {
    @Value("${profile-service-url}")
    private String profileServiceUrl;

    @Bean
    public RestClient getProfileServiceRestClient() {
        return RestClient.builder().baseUrl(profileServiceUrl).build();
    }
}
