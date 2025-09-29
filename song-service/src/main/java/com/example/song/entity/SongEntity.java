package com.example.song.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
