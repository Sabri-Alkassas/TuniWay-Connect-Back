package com.tuniway.connect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/example")
public class ExampleController {

    @GetMapping("/items")
    public ResponseEntity<List<Map<String, Object>>> getItems() {
        List<Map<String, Object>> items = List.of(
                Map.<String, Object>of("id", 1, "title", "First item", "description", "Loaded from the backend API"),
                Map.<String, Object>of("id", 2, "title", "Second item", "description", "Example data from Spring Boot"),
                Map.<String, Object>of("id", 3, "title", "Third item", "description", "Frontend and backend connected")
        );
        return ResponseEntity.ok(items);
    }
}
