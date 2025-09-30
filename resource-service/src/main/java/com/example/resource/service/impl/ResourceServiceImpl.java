package com.example.resource.service.impl;

import com.example.resource.entity.ResourceEntity;
import com.example.resource.exception.NotFoundException;
import com.example.resource.repository.ResourceRepository;
import com.example.resource.service.ResourceService;
import com.example.resource.service.SongClientService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository repository;
    private final SongClientService songClientService;

    public ResourceServiceImpl(ResourceRepository repository, SongClientService songClientService) {
        this.repository = repository;
        this.songClientService = songClientService;
    }

    @Override
    @Transactional
    public Long uploadBinary(byte[] fileBytes) {
        if (fileBytes == null) throw new IllegalArgumentException("File is required");

        ResourceEntity saved = repository.save(
                new ResourceEntity(null, fileBytes, "audio/mpeg", "uploaded.mp3")
        );

        Map<String, Object> payload = buildPayload(saved, fileBytes);
        songClientService.createSong(payload);

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
                songClientService.deleteSong(id);
                deleted.add(id);
            }
        }
        return deleted;
    }

    @Override
    public Long handleUpload(HttpServletRequest request) throws IOException {
        if (!"audio/mpeg".equals(request.getContentType())) {
            throw new IllegalArgumentException("Invalid resource upload");
        }
        return uploadBinary(request.getInputStream().readAllBytes());
    }

    @Override
    public Resource handleGet(String id) {
        return get(parseId(id));
    }

    @Override
    public List<Long> handleDelete(String id) {
        return deleteByIds(parseCsv(id));
    }

    private Map<String, Object> buildPayload(ResourceEntity saved, byte[] fileBytes) {
        Map<String, String> meta = extractMetadata(fileBytes);

        String duration = Optional.ofNullable(meta.get("duration"))
                .map(this::formatDuration)
                .orElse("00:00");

        return Map.of(
                "id", saved.getId().intValue(),
                "name", Optional.ofNullable(meta.get("name")).orElse("Unknown"),
                "artist", Optional.ofNullable(meta.get("artist")).orElse("Unknown"),
                "album", Optional.ofNullable(meta.get("album")).orElse("Unknown"),
                "duration", duration,
                "year", Optional.ofNullable(meta.get("year")).orElse("1900")
        );
    }

    private Map<String, String> extractMetadata(byte[] fileBytes) {
        Map<String, String> meta = new HashMap<>();
        try (var stream = new ByteArrayInputStream(fileBytes)) {
            AutoDetectParser parser = new AutoDetectParser();
            Metadata metadata = new Metadata();
            parser.parse(stream, new BodyContentHandler(), metadata);

            meta.put("name", metadata.get("title"));
            meta.put("artist", metadata.get("xmpDM:artist"));
            meta.put("album", metadata.get("xmpDM:album"));
            meta.put("duration", metadata.get("xmpDM:duration")); // мс
            meta.put("year", metadata.get("xmpDM:releaseDate"));
        } catch (Exception ignored) {}
        return meta;
    }

    private String formatDuration(String durMs) {
        try {
            long ms = (long) Double.parseDouble(durMs);
            long totalSec = ms / 1000;
            return String.format("%02d:%02d", totalSec / 60, totalSec % 60);
        } catch (Exception e) {
            return "00:00";
        }
    }

    private long parseId(String id) {
        try {
            long parsed = Long.parseLong(id);
            if (parsed <= 0) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
    }

    private List<Long> parseCsv(String csv) {
        if (csv.length() > 200) {
            throw new IllegalArgumentException("CSV too long: " + csv.length());
        }
        try {
            return Arrays.stream(csv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid CSV");
        }
    }
}