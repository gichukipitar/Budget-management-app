package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.service.ProfilePictureStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class LocalProfilePictureStorage implements ProfilePictureStorage {

    private final Path root = Paths.get("uploads");

    @Override
    public String save(Long userId, FilePart filePart) {
        try {
            Files.createDirectories(root);
            String filename = "user-" + userId + "-" + System.currentTimeMillis() + "-" + filePart.filename();
            Path dest = root.resolve(filename);

            // block here because your app uses blocking repositories anyway.
            filePart.transferTo(dest).block();

            // later serve this with static mapping; for now return a predictable path:
            return "/uploads/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save profile picture", e);
        }
    }
}