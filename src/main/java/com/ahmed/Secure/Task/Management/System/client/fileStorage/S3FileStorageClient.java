package com.ahmed.Secure.Task.Management.System.client.fileStorage;

import com.ahmed.Secure.Task.Management.System.system.exceptions.CustomFileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3FileStorageClient implements FileStorageClient {

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;


    @Override
    public String generateUploadUrl(String bucketName, String objectKey, String fileType, Long size, Instant expiresAt) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(fileType)
                    .contentLength(size)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .putObjectRequest(putObjectRequest)
                    .signatureDuration(java.time.Duration.between(Instant.now(), expiresAt))
                    .build();
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (S3Exception | SdkClientException exception) {
                log.error("Failed to generate upload url bucket={}, key={}", bucketName, objectKey, exception);
            throw new CustomFileStorageException("Failed to generate upload url", exception);
        }
    }

    @Override
    public String generateDownloadUrl(String bucketName, String objectKey, Instant expiresAt) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(java.time.Duration.between(Instant.now(), expiresAt))
                    .build();
            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (S3Exception | SdkClientException exception) {
            log.error("Failed to generate download url bucket={}, key={}", bucketName, objectKey, exception);
            throw new CustomFileStorageException("Failed to generate download url", exception);
        }

    }

    @Override
    public boolean fileExists(String bucketName, String objectKey) {
        try {
            s3Client.headObject(builder -> builder.bucket(bucketName).key(objectKey));
            return true;
        } catch (NoSuchKeyException exception) {
            return false; // if file doesn't exist
        } catch (S3Exception | SdkClientException exception) {
            log.error("Failed to check for object existence  bucket={}, key={}", bucketName, objectKey, exception);
            throw new CustomFileStorageException("Failed to check for object existence ", exception);        }
    }

    @Override
    public void deleteFile(String objectKey, String bucketName) {
        try {
            s3Client.deleteObject(builder -> builder.bucket(bucketName).key(objectKey));

        } catch (S3Exception | SdkClientException exception) {
            log.error("Failed to delete object  bucket={}, key={}", bucketName, objectKey, exception);
            throw new CustomFileStorageException("Failed to delete object", exception);
        }
    }
}
