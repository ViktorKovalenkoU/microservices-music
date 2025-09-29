package com.example.song.service.impl;

import com.example.song.dto.SongDto;
import com.example.song.entity.SongEntity;
import com.example.song.exception.DuplicateException;
import com.example.song.exception.NotFoundException;
import com.example.song.repository.SongRepository;
import com.example.song.service.SongService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SongServiceImpl implements SongService {

    private final SongRepository repo;

    public SongServiceImpl(SongRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public Long create(SongDto dto) {
        if (repo.existsById(dto.id())) {
            throw new DuplicateException("Song with id " + dto.id() + " already exists");
        }
        SongEntity e = toEntity(dto);
        repo.save(e);
        return e.getId();
    }

    @Override
    public SongDto get(Long id) {
        return repo.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Song metadata not found: " + id));
    }

    @Override
    @Transactional
    public List<Long> deleteByIds(List<Long> ids) {
        return ids.stream()
                .filter(repo::existsById)
                .peek(repo::deleteById)
                .toList();
    }

    @Override
    public SongDto handleGet(String id) {
        return get(parseId(id));
    }

    @Override
    public List<Long> handleDelete(String id) {
        return deleteByIds(parseCsv(id));
    }

    private SongEntity toEntity(SongDto dto) {
        SongEntity e = new SongEntity();
        e.setId(dto.id());
        e.setName(dto.name());
        e.setArtist(dto.artist());
        e.setAlbum(dto.album());
        e.setDuration(dto.duration());
        e.setYear(dto.year());
        return e;
    }

    private SongDto toDto(SongEntity e) {
        return new SongDto(e.getId(), e.getName(), e.getArtist(),
                e.getAlbum(), e.getDuration(), e.getYear());
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
                    .toList();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid CSV");
        }
    }
}