package com.ravi.hogwartsartifact.client.imageStorage.exception;

/**
 * Exception thrown when image upload to S3 fails
 */
public class ImageUploadException extends ImageStorageException {
    
    public ImageUploadException(String message) {
        super(message);
    }
    
    public ImageUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}

