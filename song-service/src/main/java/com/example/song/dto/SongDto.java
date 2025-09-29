package com.example.song.dto;

import jakarta.validation.constraints.*;

public record SongDto(
        @NotNull(message = "id is required")
        Long id,
        @NotBlank
        @Size(max = 100, message = "name must be 1-100 chars")
        String name,
        @NotBlank
        @Size(max = 100, message = "artist must be 1-100 chars")
        String artist,
        @NotBlank
        @Size(max = 100, message = "album must be 1-100 chars")
        String album,
        @Pattern(regexp = "^\\d{1,2}:[0-5]\\d$", message = "duration must be match mm:ss")
        String duration,
        @Pattern(regexp = "^(19\\d{2}|20\\d{2})$", message = "year must be valid YYYY between 1900–2099")
        String year
) {
}
