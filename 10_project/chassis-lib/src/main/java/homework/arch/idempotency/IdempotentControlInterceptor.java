package homework.arch.idempotency;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class IdempotentControlInterceptor implements HandlerInterceptor {

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    Map<String, HttpResponse> cachedResponses = new HashMap<>();

    record HttpResponse(int status, List<HttpHeader> headers, byte[] content){}
    record HttpHeader(String name, String value){ }

    // Логика, выполняемая перед выполнением контроллера
    // true - продолжить выполнение запроса, false - прервать выполнение
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // TODO: implement support of concurrent calls to control requested but not processed yet resources. To return 409 status.
        if (handler instanceof HandlerMethod handlerMethod && handlerMethod.getMethodAnnotation(Idempotent.class) != null) {
            var idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
            if (cachedResponses.containsKey(idempotencyKey)) {
                var cachedRespose = cachedResponses.get(idempotencyKey);
                response.setStatus(cachedRespose.status);
                response.getOutputStream().write(cachedRespose.content);
                cachedRespose.headers.forEach(header -> response.setHeader(header.name, header.value));
                log.debug("Cached response is used for {}: {} for idempotency purposes", request.getMethod(), request.getServletPath());
                return false;
            }
        }
        return true;
    }

    // Логика, выполняемая после выполнения контроллера, но до формирования ответа
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    // Логика, выполняемая после завершения обработки запроса и формирования ответа
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod && handlerMethod.getMethodAnnotation(Idempotent.class) != null) {
            var idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
            if (response instanceof ContentCachingResponseWrapper responseWrapper) {
                var contentAsByteArray = responseWrapper.getContentAsByteArray();
                var httpHeaders = response.getHeaderNames().stream().map(hn -> new HttpHeader(hn, response.getHeader(hn))).toList();
                cachedResponses.put(idempotencyKey, new HttpResponse(response.getStatus(), httpHeaders, contentAsByteArray));
                log.debug("Response of {}: {} is cached for provisioning of idempotency", request.getMethod(), request.getServletPath());
            } else {
                log.warn("Idempotency can't be provided due to unavailable response content. Consider wrapping HttpServletResponse to ContentCachingResponseWrapper.");
            }
        }
    }
}
