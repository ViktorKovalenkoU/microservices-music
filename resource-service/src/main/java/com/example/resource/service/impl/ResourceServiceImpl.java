package com.example.resource.service.impl;

import com.example.resource.entity.ResourceEntity;
import com.example.resource.exception.NotFoundException;
import com.example.resource.repository.ResourceRepository;
import com.example.resource.service.ResourceService;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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

        var payload = new java.util.HashMap<String,Object>();
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
        ResourceEntity e = repository.findById(id).orElseThrow(() -> new NotFoundException("Resource not found: " + id));
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
                    webClient.delete().uri(uriBuilder -> uriBuilder.path("/songs").queryParam("id", id).build())
                            .retrieve().bodyToMono(Void.class).onErrorResume(e -> reactor.core.publisher.Mono.empty()).block();
                } catch (Exception ignored) {}
                deleted.add(id);
            }
        }
        return deleted;
    }
}
