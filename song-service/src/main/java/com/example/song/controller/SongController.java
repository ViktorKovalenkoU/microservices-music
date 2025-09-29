package com.example.song.controller;

import com.example.song.dto.SongDto;
import com.example.song.service.SongService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/songs")
public class SongController {

    private final SongService service;

    public SongController(SongService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SongDto dto) {
        Long id = service.create(dto);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") String id) {
        return ResponseEntity.ok(service.handleGet(id));
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam("id") String id) {
        List<Long> deleted = service.handleDelete(id);
        return ResponseEntity.ok(Map.of("ids", deleted));
    }
}