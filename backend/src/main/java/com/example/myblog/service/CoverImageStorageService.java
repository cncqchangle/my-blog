package com.example.myblog.service;

import org.springframework.web.multipart.MultipartFile;

public interface CoverImageStorageService {

    String uploadCoverImage(String account, MultipartFile coverImage);

    String generateCoverImage(String account, String title);

    String resolveCoverImageUrl(String storedCoverImageUrl);

    String resolveCoverImageUrl(String storedCoverImageUrl, String title);

    boolean isGeneratedCoverImageUrl(String storedCoverImageUrl);

    void deleteManagedCoverImage(String storedCoverImageUrl);
}
