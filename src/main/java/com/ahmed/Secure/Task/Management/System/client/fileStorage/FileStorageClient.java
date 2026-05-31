package com.ahmed.Secure.Task.Management.System.client.fileStorage;

import java.io.InputStream;
import java.time.Instant;

public interface FileStorageClient {
    String generateUploadUrl(String containerName, String objectName, String fileType, Long size, Instant expiresAt);
    String generateDownloadUrl(String containerName, String objectName, Instant expiresAt);
    boolean fileExists(String containerName, String objectName);
    void deleteFile(String objectName, String folderName);
}
