package homework.arch.users.rest;

import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dumb")
public class DumbApiController {
    @GetMapping("/a")
    @SneakyThrows
    public String getA() {
        long millis = randomLongMs(300);
        Thread.sleep(millis);
        return Long.toString(millis);
    }

    @GetMapping("/b")
    @SneakyThrows
    public String getB() {
        long millis = randomLongMs(100);
        Thread.sleep(millis);
        return Long.toString(millis);
    }

    private static long randomLongMs(long upToMs) {
        return (long) (upToMs * Math.random());
    }
}
