package com.example.resource.controller;

import com.example.resource.service.ResourceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/resources")
public class ResourceController {
    private final ResourceService service;

    public ResourceController(ResourceService service) { this.service = service; }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadBinary(HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        byte[] fileBytes = request.getInputStream().readAllBytes();
        System.out.println("DEBUG uploadBinary: " + fileBytes.length + " bytes, type=" + contentType);

        // ❌ неправильний Content-Type → 400
        if (contentType == null || !contentType.equals("audio/mpeg")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "errorMessage", "Invalid resource upload",
                    "errorCode", "400"
            ));
        }

        // ✅ правильний Content-Type → навіть 0 байтів приймаємо
        Long id = service.uploadBinary(fileBytes);
        return ResponseEntity.ok(Map.of("id", id));
    }



    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") String id) {
        long parsed;
        try {
            parsed = Long.parseLong(id);
            if (parsed <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "errorMessage", "Invalid id: " + id,
                            "errorCode", "400"
                    ));
        }
        Resource res = service.get(parsed);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(res);
    }


    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam("id") String id) {
        if (id.length() > 200) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "errorMessage", "CSV too long: " + id.length(),
                            "errorCode", "400"
                    ));
        }

        List<Long> ids;
        try {
            ids = Arrays.stream(id.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(Map.of(
                    "errorMessage", "Invalid CSV", "errorCode", "400"
            ));
        }
        var deleted = service.deleteByIds(ids);
        return ResponseEntity.ok(Map.of("ids", deleted));
    }
}
