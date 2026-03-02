package com.sirhpitar.budget.service;

import org.springframework.http.codec.multipart.FilePart;

public interface ProfilePictureStorage {
    String save(Long userId, FilePart filePart);
}