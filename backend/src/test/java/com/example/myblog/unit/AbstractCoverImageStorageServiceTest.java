package com.example.myblog.service;

import com.example.myblog.config.OssProperties;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractCoverImageStorageServiceTest {

    @Test
    void deletesManagedCoverImageUsingExtractedObjectKey() {
        var storageService = new RecordingCoverImageStorageService();

        storageService.deleteManagedCoverImage("https://managed.example.com/my-blog/note-covers/alice/2026/04/demo.png");

        assertThat(storageService.deletedObjectKey).isEqualTo("my-blog/note-covers/alice/2026/04/demo.png");
    }

    @Test
    void skipsDefaultCoverUrlDeletion() {
        var storageService = new RecordingCoverImageStorageService();

        storageService.deleteManagedCoverImage("https://default.example.com/default-cover.png");

        assertThat(storageService.deletedObjectKey).isNull();
    }

    @Test
    void skipsExternalCoverUrlDeletion() {
        var storageService = new RecordingCoverImageStorageService();

        storageService.deleteManagedCoverImage("https://cdn.example.com/covers/demo.png");

        assertThat(storageService.deletedObjectKey).isNull();
    }

    @Test
    void resolvesMissingCoverToGeneratedSvgDataUrlWhenTitlePresent() {
        var storageService = new RecordingCoverImageStorageService();

        String resolved = storageService.resolveCoverImageUrl(null, "Streams");

        assertThat(resolved).startsWith("data:image/svg+xml;base64,");
    }

    private static final class RecordingCoverImageStorageService extends AbstractCoverImageStorageService {

        private String deletedObjectKey;

        private RecordingCoverImageStorageService() {
            super(ossProperties());
        }

        @Override
        public String uploadCoverImage(String account, MultipartFile coverImage) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String generateCoverImage(String account, String title) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isGeneratedCoverImageUrl(String storedCoverImageUrl) {
            return false;
        }

        @Override
        public void deleteManagedCoverImage(String storedCoverImageUrl) {
            String objectKey = extractManagedObjectKey(storedCoverImageUrl, Set.of("managed.example.com"));
            if (objectKey != null) {
                deleteObject(objectKey);
            }
        }

        @Override
        protected void deleteObject(String objectKey) {
            this.deletedObjectKey = objectKey;
        }

        private static OssProperties ossProperties() {
            OssProperties properties = new OssProperties();
            properties.setEndpoint("oss-cn-test.aliyuncs.com");
            properties.setBucket("test-bucket");
            properties.setAccessKeyId("key");
            properties.setAccessKeySecret("secret");
            properties.setDefaultCoverUrl("https://default.example.com/default-cover.png");
            return properties;
        }
    }
}
