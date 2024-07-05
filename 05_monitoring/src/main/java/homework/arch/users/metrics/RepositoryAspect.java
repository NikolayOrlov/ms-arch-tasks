package homework.arch.users.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static homework.arch.users.metrics.Metrics.*;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class RepositoryAspect {
    private final MeterRegistry meterRegistry;

    @Around("target(homework.arch.users.persistence.UserRepository)")
    public Object measureCall(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object proceed = null;
        try {
            proceed = pjp.proceed();
        } catch (Exception e) {
            meterRegistry.counter(pjp.getSignature().getName() + FAIL_SUFFIX).increment();
            throw e;
        }
        var processingDurationMs = System.currentTimeMillis() - startTime;
        log.info("{} done in {} ms.", pjp.getSignature(), processingDurationMs);
        meterRegistry.counter(pjp.getSignature().getName() + SUCCESS_SUFFIX).increment();
        meterRegistry.timer(pjp.getSignature().getName()).record(Duration.ofMillis(processingDurationMs));
        return proceed;
    }
}
