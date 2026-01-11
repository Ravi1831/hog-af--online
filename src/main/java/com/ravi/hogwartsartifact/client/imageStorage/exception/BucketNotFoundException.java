package com.ravi.hogwartsartifact.client.imageStorage.exception;

/**
 * Exception thrown when the specified S3 bucket does not exist
 */
public class BucketNotFoundException extends ImageStorageException {
    
    public BucketNotFoundException(String bucketName) {
        super("S3 bucket '" + bucketName + "' does not exist. Please create the bucket first.");
    }
    
    public BucketNotFoundException(String bucketName, Throwable cause) {
        super("S3 bucket '" + bucketName + "' does not exist. Please create the bucket first.", cause);
    }
}

