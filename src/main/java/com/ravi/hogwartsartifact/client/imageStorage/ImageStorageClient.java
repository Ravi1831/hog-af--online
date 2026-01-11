package com.ravi.hogwartsartifact.client.imageStorage;

import java.io.IOException;
import java.io.InputStream;

public interface ImageStorageClient {
    String uploadImage(String containerName,
                       String originalImageName,
                       InputStream inputStream,
                       long length) throws IOException;
}
