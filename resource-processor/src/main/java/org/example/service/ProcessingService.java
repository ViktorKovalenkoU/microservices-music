package org.example.service;

import org.example.client.ResourceClient;
import org.example.client.SongClient;
import org.example.dto.SongCreateRequest;
import org.example.model.SongMetadata;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;

@Service
public class ProcessingService {

    private final ResourceClient resourceClient;
    private final SongClient songClient;
    private final MetadataExtractorService extractor;

    public ProcessingService(ResourceClient resourceClient,
                             SongClient songClient,
                             MetadataExtractorService extractor) {
        this.resourceClient = resourceClient;
        this.songClient = songClient;
        this.extractor = extractor;
    }

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void process(Long resourceId) {
        try {
            byte[] binary = resourceClient.getBinary(resourceId);

            File temp = File.createTempFile("song-", ".mp3");
            Files.write(temp.toPath(), binary);

            SongMetadata meta = extractor.extractMetadata(temp);

            SongCreateRequest dto = new SongCreateRequest(
                    resourceId,
                    meta.getTitle(),
                    meta.getArtist(),
                    meta.getAlbum(),
                    meta.getDuration(),
                    null
            );

            songClient.create(dto);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to process resource " + resourceId, e
            );
        }
    }
}
