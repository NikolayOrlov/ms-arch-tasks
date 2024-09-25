package homework.arch.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class MonitoringAspect {
    private static final String CALL_SUFFIX = ".call";
    private static final String SUCCESS_SUFFIX = ".done";
    private static final String FAIL_SUFFIX = ".failed";

    private final MeterRegistry meterRegistry;

    @Around("@annotation(ExecutionMonitoring)")
    public Object measureCall(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object proceed = null;
        try {
            proceed = pjp.proceed();
        } catch (Exception ex) {
            var processingDurationMs = System.currentTimeMillis() - startTime;
            log.debug("{} throws {} due to '{}'", pjp.getSignature(), ex.getClass().getCanonicalName(), ex.getMessage());
            meterRegistry.counter(pjp.getSignature().getName() + CALL_SUFFIX + FAIL_SUFFIX).increment();
            meterRegistry.timer(pjp.getSignature().getName() + FAIL_SUFFIX).record(Duration.ofMillis(processingDurationMs));
            throw ex;
        }
        var processingDurationMs = System.currentTimeMillis() - startTime;
        log.info("{} done in {} ms.", pjp.getSignature(), processingDurationMs);
        meterRegistry.counter(pjp.getSignature().getName() + CALL_SUFFIX + SUCCESS_SUFFIX).increment();
        meterRegistry.timer(pjp.getSignature().getName() + SUCCESS_SUFFIX).record(Duration.ofMillis(processingDurationMs));
        return proceed;
    }
}
