package com.example.myblog.service;

import com.example.myblog.config.OssProperties;
import com.example.myblog.domain.exception.BadRequestException;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

abstract class AbstractCoverImageStorageService implements CoverImageStorageService {

    protected static final String UPLOADED_COVER_PREFIX = "my-blog/note-covers/";
    protected static final String GENERATED_COVER_PREFIX = "my-blog/generated-note-covers/";
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif");
    private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final OssProperties ossProperties;

    protected AbstractCoverImageStorageService(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public String resolveCoverImageUrl(String storedCoverImageUrl) {
        return resolveCoverImageUrl(storedCoverImageUrl, null);
    }

    @Override
    public String resolveCoverImageUrl(String storedCoverImageUrl, String title) {
        if (!StringUtils.hasText(storedCoverImageUrl)) {
            if (StringUtils.hasText(title)) {
                return buildGeneratedCoverDataUrl(title);
            }
            return ossProperties.getDefaultCoverUrl();
        }
        return storedCoverImageUrl;
    }

    protected byte[] generatedCoverBytes(String title) {
        return generatedCoverSvg(title).getBytes(StandardCharsets.UTF_8);
    }

    protected String generatedCoverSvg(String title) {
        String normalizedTitle = StringUtils.hasText(title) ? title.trim() : "Untitled";
        int hue = Math.floorMod(normalizedTitle.toLowerCase(Locale.ROOT).hashCode(), 360);
        String startColor = "hsl(%d, 76%%, 56%%)".formatted(hue);
        String endColor = "hsl(%d, 72%%, 34%%)".formatted((hue + 42) % 360);
        String accentColor = "hsla(%d, 100%%, 100%%, 0.16)".formatted((hue + 18) % 360);
        List<String> titleLines = buildTitleLines(normalizedTitle, 12, 2);
        StringBuilder textElements = new StringBuilder();
        int y = 300;
        for (String line : titleLines) {
            textElements.append("""
                    <text x="96" y="%d" fill="#ffffff" font-family="'Segoe UI','PingFang SC','Hiragino Sans GB','Microsoft YaHei',sans-serif" font-size="72" font-weight="700" letter-spacing="1">%s</text>
                    """.formatted(y, escapeXml(line)));
            y += 96;
        }
        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="1200" height="630" viewBox="0 0 1200 630" role="img" aria-label="%s">
                  <defs>
                    <linearGradient id="bg" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
                      <stop offset="0%%" stop-color="%s"/>
                      <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                  </defs>
                  <rect width="1200" height="630" fill="url(#bg)"/>
                  <circle cx="1000" cy="120" r="180" fill="%s"/>
                  <circle cx="920" cy="520" r="220" fill="%s"/>
                  <path d="M0 470C160 420 290 390 420 396C625 405 760 530 1200 420V630H0Z" fill="rgba(255,255,255,0.12)"/>
                  <text x="96" y="124" fill="rgba(255,255,255,0.75)" font-family="'Segoe UI','PingFang SC','Hiragino Sans GB','Microsoft YaHei',sans-serif" font-size="24" font-weight="600">MY BLOG NOTE</text>
                  %s
                </svg>
                """.formatted(
                escapeXml(normalizedTitle),
                startColor,
                endColor,
                accentColor,
                accentColor,
                textElements);
    }

    protected String generatedCoverObjectKey(String account, String title, String objectName) {
        java.time.LocalDate now = java.time.LocalDate.now();
        return GENERATED_COVER_PREFIX + "%s/%d/%02d/%s-%s.svg".formatted(
                normalizeAccountSegment(account),
                now.getYear(),
                now.getMonthValue(),
                shortHash(title),
                objectName);
    }

    protected boolean isGeneratedObjectKey(String objectKey) {
        return StringUtils.hasText(objectKey) && objectKey.startsWith(GENERATED_COVER_PREFIX);
    }

    protected String defaultCoverImageUrl() {
        return ossProperties.getDefaultCoverUrl();
    }

    protected String extractManagedObjectKey(String storedCoverImageUrl, Set<String> managedHosts) {
        if (!StringUtils.hasText(storedCoverImageUrl) || defaultCoverImageUrl().equals(storedCoverImageUrl)) {
            return null;
        }

        try {
            URI uri = new URI(storedCoverImageUrl);
            String host = uri.getHost();
            if (!StringUtils.hasText(host)) {
                return null;
            }

            boolean managedHost = managedHosts.stream()
                    .anyMatch(candidate -> candidate.equalsIgnoreCase(host));
            if (!managedHost) {
                return null;
            }

            String path = uri.getPath();
            if (!StringUtils.hasText(path) || "/".equals(path)) {
                return null;
            }
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    protected String normalizeAccountSegment(String account) {
        return account == null ? "anonymous" : account.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
    }

    protected String shortHash(String value) {
        String normalized = StringUtils.hasText(value) ? value.trim() : "untitled";
        return Integer.toHexString(normalized.toLowerCase(Locale.ROOT).hashCode());
    }

    protected String detectExtension(MultipartFile coverImage) {
        validateFile(coverImage);

        String filenameExtension = StringUtils.getFilenameExtension(coverImage.getOriginalFilename());
        if (StringUtils.hasText(filenameExtension)) {
            String normalized = filenameExtension.toLowerCase(Locale.ROOT);
            if ("jpeg".equals(normalized)) {
                return "jpg";
            }
            if (ALLOWED_EXTENSIONS.contains(normalized)) {
                return normalized;
            }
        }

        String contentType = coverImage.getContentType();
        if (StringUtils.hasText(contentType) && EXTENSION_BY_CONTENT_TYPE.containsKey(contentType)) {
            return EXTENSION_BY_CONTENT_TYPE.get(contentType);
        }

        throw new BadRequestException("Cover image type is not supported");
    }

    private void validateFile(MultipartFile coverImage) {
        if (coverImage == null || coverImage.isEmpty()) {
            throw new BadRequestException("Cover image file is required");
        }
        if (coverImage.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Cover image must be no larger than 5 MB");
        }

        String contentType = coverImage.getContentType();
        String filenameExtension = StringUtils.getFilenameExtension(coverImage.getOriginalFilename());
        boolean supportedContentType = StringUtils.hasText(contentType) && ALLOWED_CONTENT_TYPES.contains(contentType);
        boolean supportedExtension = StringUtils.hasText(filenameExtension)
                && ALLOWED_EXTENSIONS.contains(filenameExtension.toLowerCase(Locale.ROOT));
        if (!supportedContentType && !supportedExtension) {
            throw new BadRequestException("Cover image type is not supported");
        }
    }

    private String buildGeneratedCoverDataUrl(String title) {
        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(generatedCoverBytes(title));
    }

    private List<String> buildTitleLines(String title, int lineLength, int maxLines) {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        int start = 0;
        int totalCodePoints = title.codePointCount(0, title.length());
        for (int lineIndex = 0; lineIndex < maxLines && start < totalCodePoints; lineIndex++) {
            boolean lastLine = lineIndex == maxLines - 1;
            int remaining = totalCodePoints - start;
            int targetLength = Math.min(lineLength, remaining);
            int end = start + targetLength;
            boolean truncated = lastLine && remaining > lineLength;
            if (truncated && targetLength > 3) {
                end = start + targetLength - 3;
            }
            String line = substringByCodePoints(title, start, end);
            if (truncated) {
                line += "...";
            }
            lines.add(line);
            start += targetLength;
            if (truncated) {
                break;
            }
        }
        if (lines.isEmpty()) {
            lines.add("Untitled");
        }
        return lines;
    }

    private String substringByCodePoints(String value, int beginCodePoint, int endCodePoint) {
        int beginIndex = value.offsetByCodePoints(0, beginCodePoint);
        int endIndex = value.offsetByCodePoints(0, endCodePoint);
        return value.substring(beginIndex, endIndex);
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    protected abstract void deleteObject(String objectKey);
}
