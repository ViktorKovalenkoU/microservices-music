package com.example.resource.service.impl;

import com.example.resource.entity.ResourceEntity;
import com.example.resource.exception.NotFoundException;
import com.example.resource.repository.ResourceRepository;
import com.example.resource.service.ResourceService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository repository;
    private final WebClient webClient;

    public ResourceServiceImpl(ResourceRepository repository, WebClient.Builder webClientBuilder) {
        this.repository = repository;
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    @Override
    @Transactional
    public Long uploadBinary(byte[] fileBytes) {
        if (fileBytes == null) {
            throw new IllegalArgumentException("File is required");
        }

        ResourceEntity ent = new ResourceEntity();
        ent.setData(fileBytes);
        ent.setContentType("audio/mpeg");
        ent.setFileName("uploaded.mp3");
        ResourceEntity saved = repository.save(ent);

        var payload = new HashMap<String, Object>();
        payload.put("id", saved.getId().intValue());
        payload.put("name", "");
        payload.put("artist", "");
        payload.put("album", "");
        payload.put("duration", "00:00");
        payload.put("year", "1900");

        try {
            webClient.post().uri("/songs")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(e -> reactor.core.publisher.Mono.empty())
                    .block();
        } catch (Exception ignored) {}

        return saved.getId();
    }

    @Override
    public Resource get(Long id) {
        ResourceEntity e = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found: " + id));
        return new ByteArrayResource(e.getData());
    }

    @Override
    @Transactional
    public List<Long> deleteByIds(List<Long> ids) {
        List<Long> deleted = new ArrayList<>();
        for (Long id : ids) {
            if (repository.existsById(id)) {
                repository.deleteById(id);
                try {
                    webClient.delete()
                            .uri(uriBuilder -> uriBuilder.path("/songs").queryParam("id", id).build())
                            .retrieve()
                            .bodyToMono(Void.class)
                            .onErrorResume(e -> reactor.core.publisher.Mono.empty())
                            .block();
                } catch (Exception ignored) {}
                deleted.add(id);
            }
        }
        return deleted;
    }


    @Override
    public Long handleUpload(HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        byte[] fileBytes = request.getInputStream().readAllBytes();

        if (contentType == null || !contentType.equals("audio/mpeg")) {
            throw new IllegalArgumentException("Invalid resource upload");
        }

        return uploadBinary(fileBytes);
    }

    @Override
    public Resource handleGet(String id) {
        long parsed;
        try {
            parsed = Long.parseLong(id);
            if (parsed <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
        return get(parsed);
    }

    @Override
    public List<Long> handleDelete(String id) {
        if (id.length() > 200) {
            throw new IllegalArgumentException("CSV too long: " + id.length());
        }

        List<Long> ids;
        try {
            ids = Arrays.stream(id.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid CSV");
        }
        return deleteByIds(ids);
    }
}