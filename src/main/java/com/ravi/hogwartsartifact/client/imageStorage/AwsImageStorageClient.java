package com.ravi.hogwartsartifact.client.imageStorage;

import com.ravi.hogwartsartifact.client.imageStorage.exception.*;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class AwsImageStorageClient implements ImageStorageClient {

    private static final Logger logger = LoggerFactory.getLogger(AwsImageStorageClient.class);

    // Allowed image file extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg"
    );

    // Maximum file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final S3Template s3Template;

    @Value("${aws.s3.max-file-size:10485760}") // Default 10MB
    private long maxFileSize;

    public AwsImageStorageClient(S3Template s3Template) {
        this.s3Template = s3Template;
    }

    @Override
    public String uploadImage(String containerName, String originalImageName, InputStream inputStream, long length) throws IOException {
        logger.info("Starting image upload to bucket: {}, original filename: {}, size: {} bytes",
                    containerName, originalImageName, length);

        // Validate inputs
        validateUploadRequest(containerName, originalImageName, length);

        try {
            // Generate unique filename with original extension
            String newImageName = generateUniqueFileName(originalImageName);
            logger.debug("Generated unique filename: {}", newImageName);

            // Upload to S3
            S3Resource resource = s3Template.upload(containerName, newImageName, inputStream);
            String imageUrl = resource.getURL().toString();

            logger.info("Successfully uploaded image to S3. URL: {}", imageUrl);
            return imageUrl;

        } catch (NoSuchBucketException e) {
            logger.error("S3 bucket '{}' not found", containerName, e);
            throw new BucketNotFoundException(containerName, e);

        } catch (S3Exception e) {
            logger.error("S3 service error during upload to bucket '{}': {}",
                        containerName, e.awsErrorDetails().errorMessage(), e);
            throw new ImageUploadException(
                    "Failed to upload image to S3: " + e.awsErrorDetails().errorMessage(), e);

        } catch (SdkClientException e) {
            logger.error("S3 connection error during upload to bucket '{}'", containerName, e);
            throw new S3ConnectionException(
                    "Unable to connect to S3 service. Please check if LocalStack/S3 is running.", e);

        } catch (IOException e) {
            logger.error("IO error during image upload to bucket '{}'", containerName, e);
            throw new ImageUploadException("Failed to read image file during upload", e);

        } catch (Exception e) {
            logger.error("Unexpected error during image upload to bucket '{}'", containerName, e);
            throw new ImageUploadException("Unexpected error occurred during image upload: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the upload request parameters
     */
    private void validateUploadRequest(String containerName, String originalImageName, long fileSize) {
        // Validate bucket name
        if (containerName == null || containerName.trim().isEmpty()) {
            throw new InvalidFileException("Bucket name cannot be null or empty");
        }

        // Validate filename
        if (originalImageName == null || originalImageName.trim().isEmpty()) {
            throw new InvalidFileException("File name cannot be null or empty");
        }

        // Validate file size
        if (fileSize <= 0) {
            throw new InvalidFileException("File is empty or size is invalid");
        }

        if (fileSize > maxFileSize) {
            throw new InvalidFileException(
                    String.format("File size (%d bytes) exceeds maximum allowed size (%d bytes / %.2f MB)",
                                fileSize, maxFileSize, maxFileSize / (1024.0 * 1024.0)));
        }

        // Validate file extension
        String fileExtension = getFileExtension(originalImageName);
        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new InvalidFileException(
                    String.format("File type '%s' is not allowed. Allowed types: %s",
                                fileExtension, ALLOWED_EXTENSIONS));
        }

        logger.debug("Upload request validation passed for file: {}", originalImageName);
    }

    /**
     * Generates a unique filename preserving the original extension
     */
    private String generateUniqueFileName(String originalImageName) {
        String extension = getFileExtension(originalImageName);
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Extracts file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            throw new InvalidFileException("File must have a valid extension");
        }
        return filename.substring(lastDotIndex);
    }
}
