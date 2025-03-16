package project.coca.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HttpController {
    @GetMapping("/api/healthcheck")
    public String healthcheck() {
        log.info("healthcheck");
        return "OK";
    }
}
