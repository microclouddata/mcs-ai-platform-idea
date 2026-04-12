package com.mcs.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Circuit-breaker fallback endpoint.
 * Returned when the backend service is unreachable or open-circuits.
 */
@RestController
public class FallbackController {

    @RequestMapping("/fallback")
    public ResponseEntity<Map<String, String>> fallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "service_unavailable",
                "message", "The backend service is temporarily unavailable. Please try again later."
        ));
    }
}
