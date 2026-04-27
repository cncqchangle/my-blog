package com.example.myblog.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.example.myblog.config.OssProperties;
import com.example.myblog.domain.exception.BadRequestException;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("!test")
public class OssCoverImageStorageService extends AbstractCoverImageStorageService {

    private final OssProperties ossProperties;

    public OssCoverImageStorageService(OssProperties ossProperties) {
        super(ossProperties);
        this.ossProperties = ossProperties;
    }

    @Override
    public String uploadCoverImage(String account, MultipartFile coverImage) {
        String extension = detectExtension(coverImage);
        LocalDate now = LocalDate.now();
        String objectKey = UPLOADED_COVER_PREFIX + "%s/%d/%02d/%s.%s".formatted(
                normalizeAccountSegment(account),
                now.getYear(),
                now.getMonthValue(),
                UUID.randomUUID(),
                extension);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(coverImage.getSize());
        if (StringUtils.hasText(coverImage.getContentType())) {
            metadata.setContentType(coverImage.getContentType());
        }

        OSS client = new OSSClientBuilder().build(
                normalizeEndpoint(ossProperties.getEndpoint()),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret());
        try (var inputStream = coverImage.getInputStream()) {
            client.putObject(ossProperties.getBucket(), objectKey, inputStream, metadata);
            return buildPublicUrl(objectKey);
        } catch (IOException ex) {
            throw new BadRequestException("Failed to read cover image");
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Failed to upload cover image to OSS", ex);
        } finally {
            client.shutdown();
        }
    }

    @Override
    public String generateCoverImage(String account, String title) {
        byte[] imageBytes = generatedCoverBytes(title);
        String objectKey = generatedCoverObjectKey(account, title, UUID.randomUUID().toString());

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(imageBytes.length);
        metadata.setContentType("image/svg+xml");

        OSS client = new OSSClientBuilder().build(
                normalizeEndpoint(ossProperties.getEndpoint()),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret());
        try {
            var inputStream = new ByteArrayInputStream(imageBytes);
            client.putObject(ossProperties.getBucket(), objectKey, inputStream, metadata);
            return buildPublicUrl(objectKey);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Failed to upload generated cover image to OSS", ex);
        } finally {
            client.shutdown();
        }
    }

    @Override
    public boolean isGeneratedCoverImageUrl(String storedCoverImageUrl) {
        String objectKey = extractManagedObjectKey(storedCoverImageUrl, Set.of(publicBucketHost()));
        return isGeneratedObjectKey(objectKey);
    }

    @Override
    public void deleteManagedCoverImage(String storedCoverImageUrl) {
        String objectKey = extractManagedObjectKey(storedCoverImageUrl, Set.of(publicBucketHost()));
        if (!StringUtils.hasText(objectKey)) {
            return;
        }
        deleteObject(objectKey);
    }

    private String buildPublicUrl(String objectKey) {
        return "https://" + publicBucketHost() + "/" + objectKey;
    }

    private String normalizeEndpoint(String endpoint) {
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        return "https://" + endpoint;
    }

    private String publicBucketHost() {
        String endpoint = normalizeEndpoint(ossProperties.getEndpoint());
        String host = endpoint.replaceFirst("^https?://", "").replaceAll("/+$", "");
        if (!host.startsWith(ossProperties.getBucket() + ".")) {
            host = ossProperties.getBucket() + "." + host;
        }
        return host;
    }

    @Override
    protected void deleteObject(String objectKey) {
        OSS client = new OSSClientBuilder().build(
                normalizeEndpoint(ossProperties.getEndpoint()),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret());
        try {
            client.deleteObject(ossProperties.getBucket(), objectKey);
        } finally {
            client.shutdown();
        }
    }
}
