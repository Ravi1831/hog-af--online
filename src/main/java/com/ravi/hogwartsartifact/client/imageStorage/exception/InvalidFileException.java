package com.ravi.hogwartsartifact.client.imageStorage.exception;

/**
 * Exception thrown when the file is invalid (wrong type, too large, empty, etc.)
 */
public class InvalidFileException extends ImageStorageException {
    
    public InvalidFileException(String message) {
        super(message);
    }
    
    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}

