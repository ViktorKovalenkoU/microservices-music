package org.example.client;

import org.example.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "resource-service",
        configuration = FeignConfig.class
)
public interface ResourceClient {


    @GetMapping(value = "/resources/{id}", produces = "audio/mpeg")
    byte[] getBinary(@PathVariable("id") Long id);
}

