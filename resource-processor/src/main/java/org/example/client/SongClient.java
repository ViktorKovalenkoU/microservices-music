package org.example.client;

import org.example.config.FeignConfig;
import org.example.dto.SongCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "song-service",
        configuration = FeignConfig.class
)
public interface SongClient {
    @PostMapping("/songs")
    Long create(@RequestBody SongCreateRequest request);
}

