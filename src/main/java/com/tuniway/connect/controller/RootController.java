package com.tuniway.connect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "service", "TuniWay Connect Backend",
                "status", "UP",
                "docs", "API: GET /api/health, GET /api/example/items"
        ));
    }
}
