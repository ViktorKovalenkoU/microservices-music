package com.example.resource.controller;

import com.example.resource.service.S3StorageService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/files")
public class FileController {

    private final S3StorageService s3;

    public FileController(S3StorageService s3) {
        this.s3 = s3;
    }

    @PostMapping("/{key}")
    public String upload(@PathVariable String key, @RequestBody String body) {
        s3.upload(key, body.getBytes());
        return "Uploaded";
    }

    @GetMapping("/{key}")
    public String download(@PathVariable String key) throws Exception {
        return new String(s3.download(key));
    }
}

