package com.ravi.hogwartsartifact.client.imageStorage.exception;

/**
 * Exception thrown when connection to S3 service fails
 */
public class S3ConnectionException extends ImageStorageException {
    
    public S3ConnectionException(String message) {
        super(message);
    }
    
    public S3ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

