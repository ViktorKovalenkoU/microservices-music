package com.example.resource.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public interface ResourceService {

    Long handleUpload(HttpServletRequest request) throws IOException;

    Resource handleGet(String id);

    List<Long> handleDelete(String id);
}