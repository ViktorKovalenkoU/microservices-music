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
import java.util.stream.Collectors;

@Service
public class SongServiceImpl implements SongService {

    private final SongRepository repo;
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
        if (dto.duration() == null || !dto.duration().matches("^\\d{1,2}:[0-5]\\d$")) {
            errors.put("duration", "must match mm:ss");
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

    @Override
    public SongDto handleGet(String id) {
        long parsed;
        try {
            parsed = Long.parseLong(id);
            if (parsed <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
        return get(parsed);
    }

    @Override
    public List<Long> handleDelete(String id) {
        if (id.length() > 200) {
            throw new IllegalArgumentException("CSV too long: " + id.length());
        }
        List<Long> ids;
        try {
            ids = Arrays.stream(id.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid CSV");
        }
        return deleteByIds(ids);
    }
}