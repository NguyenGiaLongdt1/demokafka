package com.example.demo.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/practice/limited")
public class RateLimitController {

    private final ApiRateLimitService rateLimitService;

    public RateLimitController(ApiRateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello(HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        if (!rateLimitService.allowRequest(ip)) {
            return ResponseEntity.status(429)
                    .body("Qua 5 request trong 60 giay. Thu lai sau.");
        }

        return ResponseEntity.ok("Hello! Request duoc chap nhan.");
    }
}