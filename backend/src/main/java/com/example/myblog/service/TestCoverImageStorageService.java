package com.example.myblog.service;

import com.example.myblog.config.OssProperties;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("test")
public class TestCoverImageStorageService extends AbstractCoverImageStorageService {

    private static final String TEST_BUCKET_HOST = "test-bucket.example.com";

    public TestCoverImageStorageService(OssProperties ossProperties) {
        super(ossProperties);
    }

    @Override
    public String uploadCoverImage(String account, MultipartFile coverImage) {
        String extension = detectExtension(coverImage);
        LocalDate now = LocalDate.now();
        return "https://test-bucket.example.com/" + UPLOADED_COVER_PREFIX + "%s/%d/%02d/%s.%s".formatted(
                normalizeAccountSegment(account),
                now.getYear(),
                now.getMonthValue(),
                UUID.randomUUID(),
                extension);
    }

    @Override
    public String generateCoverImage(String account, String title) {
        LocalDate now = LocalDate.now();
        return "https://test-bucket.example.com/" + GENERATED_COVER_PREFIX + "%s/%d/%02d/%s-%s.svg".formatted(
                normalizeAccountSegment(account),
                now.getYear(),
                now.getMonthValue(),
                shortHash(title),
                UUID.randomUUID());
    }

    @Override
    public boolean isGeneratedCoverImageUrl(String storedCoverImageUrl) {
        String objectKey = extractManagedObjectKey(storedCoverImageUrl, Set.of(TEST_BUCKET_HOST));
        return isGeneratedObjectKey(objectKey);
    }

    @Override
    public void deleteManagedCoverImage(String storedCoverImageUrl) {
        String objectKey = extractManagedObjectKey(storedCoverImageUrl, Set.of(TEST_BUCKET_HOST));
        if (!StringUtils.hasText(objectKey)) {
            return;
        }
        deleteObject(objectKey);
    }

    @Override
    protected void deleteObject(String objectKey) {
        // Test profile keeps storage interactions in-memory/no-op.
    }
}
