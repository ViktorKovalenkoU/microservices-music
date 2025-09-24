package com.example.song.controller;

import com.example.song.dto.SongDto;
import com.example.song.service.SongService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/songs")
public class SongController {
    private final SongService service;

    public SongController(SongService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SongDto dto) {
        Long id = service.create(dto);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") String id) {
        long parsed;
        try {
            parsed = Long.parseLong(id);
            if (parsed <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "errorMessage", "Invalid id: " + id,
                    "errorCode", "400"
            ));
        }
        return ResponseEntity.ok(service.get(parsed));
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam("id") String id) {
        if (id.length() > 200) {
            return ResponseEntity.badRequest().body(Map.of(
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
            return ResponseEntity.badRequest().body(Map.of(
                    "errorMessage", "Invalid CSV",
                    "errorCode", "400"
            ));
        }
        var deleted = service.deleteByIds(ids);
        return ResponseEntity.ok(Map.of("ids", deleted));
    }
}
