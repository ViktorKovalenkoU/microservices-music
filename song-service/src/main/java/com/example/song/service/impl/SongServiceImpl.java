package com.example.song.service.impl;

import com.example.song.dto.SongDto;
import com.example.song.entity.SongEntity;
import com.example.song.exception.DuplicateException;
import com.example.song.exception.NotFoundException;
import com.example.song.exception.ValidationErrorsException;
import com.example.song.repository.SongRepository;
import com.example.song.service.SongService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class SongServiceImpl implements SongService {
    private final SongRepository repo;
    private final Pattern durationPattern = Pattern.compile("^\\d{1,2}:[0-5]\\d$");
    private final Pattern yearPattern = Pattern.compile("^(19\\d{2}|20\\d{2})$");

    public SongServiceImpl(SongRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public Long create(SongDto dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto.id() == null) {
            errors.put("id", "id is required");
        } else if (repo.existsById(dto.id())) {
            // дубль → 409
            throw new DuplicateException("Song with id " + dto.id() + " already exists");
        }

        if (dto.name() == null || dto.name().length() > 100) {
            errors.put("name", "must be 1-100 chars");
        }
        if (dto.artist() == null || dto.artist().length() > 100) {
            errors.put("artist", "must be 1-100 chars");
        }
        if (dto.album() == null || dto.album().length() > 100) {
            errors.put("album", "must be 1-100 chars");
        }
        if (dto.duration() == null) {
            errors.put("duration", "must match mm:ss");
        } else {
            String[] parts = dto.duration().split(":");
            if (parts.length != 2) {
                errors.put("duration", "must match mm:ss");
            } else {
                try {
                    int minutes = Integer.parseInt(parts[0]);
                    int seconds = Integer.parseInt(parts[1]);
                    if (minutes < 0 || seconds < 0 || seconds > 59) {
                        errors.put("duration", "must match mm:ss");
                    }
                } catch (NumberFormatException e) {
                    errors.put("duration", "must match mm:ss");
                }
            }
        }
        if (dto.year() == null || !yearPattern.matcher(dto.year()).matches()) {
            errors.put("year", "must be valid YYYY between 1900–2099");
        }
        if (!errors.isEmpty()) {
            throw new ValidationErrorsException(errors);
        }


        SongEntity e = new SongEntity();
        e.setId(dto.id());
        e.setName(dto.name());
        e.setArtist(dto.artist());
        e.setAlbum(dto.album());
        e.setDuration(dto.duration());
        e.setYear(dto.year());
        repo.save(e);
        return e.getId();
    }

    @Override
    public SongDto get(Long id) {
        SongEntity e = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Song metadata not found: " + id));
        return new SongDto(e.getId(), e.getName(), e.getArtist(),
                e.getAlbum(), e.getDuration(), e.getYear());
    }

    @Override
    @Transactional
    public List<Long> deleteByIds(List<Long> ids) {
        List<Long> deleted = new ArrayList<>();
        for (Long id : ids) {
            if (repo.existsById(id)) {
                repo.deleteById(id);
                deleted.add(id);
            }
        }
        return deleted;
    }
}