package com.keepcalm.placementportal.service;

import org.springframework.web.multipart.MultipartFile;

public interface ObjectStorageService {
    String store(String namespace, MultipartFile file);
    void delete(String objectKey);
}
