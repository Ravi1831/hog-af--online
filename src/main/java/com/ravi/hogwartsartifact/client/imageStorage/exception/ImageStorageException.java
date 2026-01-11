package com.ravi.hogwartsartifact.client.imageStorage.exception;

/**
 * Base exception for all image storage related errors
 */
public class ImageStorageException extends RuntimeException {
    
    public ImageStorageException(String message) {
        super(message);
    }
    
    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

