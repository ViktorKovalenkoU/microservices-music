package com.example.resource.controller;

import com.example.resource.service.ResourceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService service;

    public ResourceController(ResourceService service) {
        this.service = service;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadBinary(HttpServletRequest request) throws IOException {
        Long id = service.handleUpload(request);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") String id) {
        Resource res = service.handleGet(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(res);
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam("id") String id) {
        List<Long> deleted = service.handleDelete(id);
        return ResponseEntity.ok(Map.of("ids", deleted));
    }
}