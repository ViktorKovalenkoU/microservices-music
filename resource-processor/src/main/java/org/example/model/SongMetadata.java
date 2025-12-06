package org.example.model;

import lombok.Data;

@Data
public class SongMetadata {
    private String title;
    private String artist;
    private String album;
    private String duration;
    private String contentType;
}

