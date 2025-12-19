package com.example.resource.service.impl;

import com.example.resource.entity.ResourceEntity;
import com.example.resource.exception.NotFoundException;
import com.example.resource.repository.ResourceRepository;
import com.example.resource.service.ResourceService;
import com.example.resource.service.S3StorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository repository;
    private final S3StorageService s3;

    public ResourceServiceImpl(ResourceRepository repository,
                               S3StorageService s3) {
        this.repository = repository;
        this.s3 = s3;
    }

    @Override
    @Transactional
    public Long handleUpload(HttpServletRequest request) throws IOException {

        String contentType = request.getContentType();
        if (contentType == null || !contentType.startsWith("audio")) {
            throw new IllegalArgumentException("Invalid resource upload");
        }

        byte[] fileBytes = request.getInputStream().readAllBytes();
        if (fileBytes.length == 0) {
            throw new IllegalArgumentException("Empty file upload");
        }

        String storageKey = UUID.randomUUID() + ".mp3";

        s3.upload(storageKey, fileBytes);

        ResourceEntity saved = repository.save(
                new ResourceEntity(
                        null,
                        storageKey,
                        contentType,
                        "uploaded.mp3"
                )
        );

        return saved.getId();
    }



    @Override
    public Resource handleGet(String id) {
        long parsedId = parseId(id);

        ResourceEntity entity = repository.findById(parsedId)
                .orElseThrow(() -> new NotFoundException(
                        "Resource not found: " + parsedId
                ));

        try {
            byte[] data = s3.download(entity.getStorageKey());
            return new ByteArrayResource(data);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read resource from storage", e
            );
        }
    }

    @Override
    @Transactional
    public List<Long> handleDelete(String id) {
        List<Long> ids = parseCsv(id);
        List<Long> deleted = new ArrayList<>();

        for (Long resourceId : ids) {
            repository.findById(resourceId).ifPresent(entity -> {
                s3.delete(entity.getStorageKey());
                repository.delete(entity);
                deleted.add(resourceId);
            });
        }
        return deleted;
    }

    private long parseId(String id) {
        try {
            long parsed = Long.parseLong(id);
            if (parsed <= 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
    }

    private List<Long> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            throw new IllegalArgumentException("ID list is empty");
        }
        if (csv.length() > 200) {
            throw new IllegalArgumentException("CSV too long");
        }

        try {
            return Arrays.stream(csv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid CSV format");
        }
    }
}
