package com.example.resource.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class S3StorageService {
    @Value("${aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 s3;

    public S3StorageService(AmazonS3 s3) {
        this.s3 = s3;
    }

    @PostConstruct
    public void init() {
        if (!s3.doesBucketExistV2(bucket)) {
            s3.createBucket(bucket);
        }
    }

    public void upload(String key, byte[] content) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.length);

        s3.putObject(bucket, key, new ByteArrayInputStream(content), metadata);
    }

    public byte[] download(String key) throws IOException {
        S3Object obj = s3.getObject(bucket, key);
        try (InputStream is = obj.getObjectContent()) {
            return IOUtils.toByteArray(is);
        }
    }

    public void delete(String key) {
        s3.deleteObject(bucket, key);
    }
}
