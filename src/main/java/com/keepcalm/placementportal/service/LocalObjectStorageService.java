package com.keepcalm.placementportal.service;

import com.keepcalm.placementportal.exception.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalObjectStorageService implements ObjectStorageService {
    private final Path root;

    public LocalObjectStorageService(@Value("${storage.local.root}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    @Override
    public String store(String namespace, MultipartFile file) {
        String original = file.getOriginalFilename() == null ? "document" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT) : "";
        String key = namespace + "/" + UUID.randomUUID() + extension;
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) throw new DomainException(HttpStatus.BAD_REQUEST, "INVALID_FILE_NAME", "Invalid file name");
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
            return key.replace('\\', '/');
        } catch (IOException ex) {
            throw new DomainException(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_FAILED", "Document could not be stored");
        }
    }

    @Override
    public void delete(String objectKey) {
        Path target = root.resolve(objectKey).normalize();
        if (!target.startsWith(root)) return;
        try { Files.deleteIfExists(target); }
        catch (IOException ex) { throw new DomainException(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_FAILED", "Document could not be removed"); }
    }
}
