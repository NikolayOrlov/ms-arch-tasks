package homework.arch.users.metrics;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "management.metrics")
@Data
public class MetricsProperties {
    private Tags tags;
    record Tags(String application) { }
}
