package putrack.server.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Root", description = "홈")
public class HelloController {
    @GetMapping("/")
    public String hello() {
        return "Hello, PU-Track!";
    }
}
