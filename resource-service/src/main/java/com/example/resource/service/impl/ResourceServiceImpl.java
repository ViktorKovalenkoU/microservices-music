package com.example.resource.service.impl;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import com.example.resource.entity.ResourceEntity;
import com.example.resource.exception.NotFoundException;
import com.example.resource.repository.ResourceRepository;
import com.example.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Value;
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

    public ResourceServiceImpl(ResourceRepository repository, WebClient.Builder webClientBuilder,
                               @Value("${resource.service.base-url}") String baseUrl) {
        this.repository = repository;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
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

        Map<String, String> meta = extractMetadata(fileBytes);

        var payload = new HashMap<String, Object>();
        payload.put("id", saved.getId().intValue());
        payload.put("name", Optional.ofNullable(meta.get("name")).orElse("Unknown"));
        payload.put("artist", Optional.ofNullable(meta.get("artist")).orElse("Unknown"));
        payload.put("album", Optional.ofNullable(meta.get("album")).orElse("Unknown"));

        String duration = "00:00";
        try {
            String durMs = meta.get("duration");
            if (durMs != null) {
                long ms = (long) Double.parseDouble(durMs);
                long totalSec = ms / 1000;
                long min = totalSec / 60;
                long sec = totalSec % 60;
                duration = String.format("%02d:%02d", min, sec);
            }
        } catch (Exception ignored) {}
        payload.put("duration", duration);

        payload.put("year", Optional.ofNullable(meta.get("year")).orElse("1900"));

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
                } catch (Exception ignored) {
                }
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

    private Map<String, String> extractMetadata(byte[] fileBytes) {
        Map<String, String> meta = new HashMap<>();
        try (var stream = new java.io.ByteArrayInputStream(fileBytes)) {
            AutoDetectParser parser = new AutoDetectParser();
            Metadata metadata = new Metadata();
            BodyContentHandler handler = new BodyContentHandler();

            parser.parse(stream, handler, metadata);

            meta.put("name", metadata.get("title"));
            meta.put("artist", metadata.get("xmpDM:artist"));
            meta.put("album", metadata.get("xmpDM:album"));
            meta.put("duration", metadata.get("xmpDM:duration"));
            meta.put("year", metadata.get("xmpDM:releaseDate"));
        } catch (Exception e) {
        }
        return meta;
    }
}