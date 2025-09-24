package com.example.song.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "songs")
public class SongEntity {
    @Id
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 100, nullable = false)
    private String artist;

    @Column(length = 100, nullable = false)
    private String album;

    @Column(length = 5, nullable = false)
    private String duration;

    @Column(length = 4, nullable = false)
    private String year;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
}
