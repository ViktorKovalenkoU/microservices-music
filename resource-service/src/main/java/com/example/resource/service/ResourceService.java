package com.example.resource.service;

import org.springframework.core.io.Resource;

import java.util.List;

public interface ResourceService {
    Resource get(Long id);
    List<Long> deleteByIds(List<Long> ids);
    Long uploadBinary(byte[] fileBytes);

}
