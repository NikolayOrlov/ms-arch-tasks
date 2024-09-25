package homework.arch.idempotency;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class IdempotencyWebConfig {

    @Bean
    public WebMvcConfigurer idempotencyWebConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new IdempotentControlInterceptor());
            }
        };
    }

    @Bean
    public FilterRegistrationBean<ContentCachingFilter> filterRegistrationBeanFotContentCachingFilter()
    {
        FilterRegistrationBean<ContentCachingFilter> bean = new FilterRegistrationBean<>();

        bean.setFilter(new ContentCachingFilter());
        bean.addUrlPatterns("/*");

        return bean;
    }
}
