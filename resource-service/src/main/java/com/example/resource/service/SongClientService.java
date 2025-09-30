package com.example.resource.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
public class SongClientService {

    private final WebClient webClient;

    public SongClientService(WebClient.Builder builder,
                             @org.springframework.beans.factory.annotation.Value("${resource.service.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public void createSong(Map<String, Object> payload) {
        try {
            webClient.post().uri("/songs")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(e -> Mono.empty())
                    .block();
        } catch (Exception ignored) {}
    }

    public void deleteSong(Long id) {
        try {
            webClient.delete()
                    .uri(uriBuilder -> uriBuilder.path("/songs").queryParam("id", id).build())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .onErrorResume(e -> Mono.empty())
                    .block();
        } catch (Exception ignored) {}
    }
}