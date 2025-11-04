package com.example.resource.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
public class SongClientService {

    private final WebClient webClient;
    private final Logger logger = LoggerFactory.getLogger(SongClientService.class);

    public SongClientService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://song-service").build();
    }

    public void createSong(Map<String, Object> payload) {
        try {
            webClient.post()
                    .uri("/songs")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(e -> {
                        logger.error("Error creating song: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void deleteSong(Long id) {
        try {
            webClient.delete()
                    .uri(uriBuilder -> uriBuilder.path("/songs").queryParam("id", id).build())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(e -> {
                        logger.error("Error deleting song: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
