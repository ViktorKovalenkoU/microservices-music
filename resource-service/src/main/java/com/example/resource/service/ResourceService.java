package com.example.resource.service;

import org.springframework.core.io.Resource;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public interface ResourceService {
    Long uploadBinary(byte[] fileBytes);
    Resource get(Long id);
    List<Long> deleteByIds(List<Long> ids);

    Long handleUpload(HttpServletRequest request) throws IOException;
    Resource handleGet(String id);
    List<Long> handleDelete(String id);
}