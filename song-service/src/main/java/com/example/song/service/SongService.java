package com.example.song.service;

import com.example.song.dto.SongDto;
import java.util.List;

public interface SongService {
    Long create(SongDto dto);
    SongDto get(Long id);
    List<Long> deleteByIds(List<Long> ids);

    SongDto handleGet(String id);
    List<Long> handleDelete(String id);
}