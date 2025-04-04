package it.softwaredoctor.emailstorage.service;

import java.io.File;
import java.net.URL;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.S3Client;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    public String uploadFile(File file) {
        String key = UUID.randomUUID() + "_" + file.getName();
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
            return getFileUrl(key);
        } catch (S3Exception e) {
            throw new IllegalStateException("Error uploading file to S3", e);
        }
    }

    private String getFileUrl(String key) {
        URL fileUrl = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(key));
        return fileUrl.toString();
    }
}

/*
    public String uploadFile(File file, String fileName) throws IOException {
        String key = UUID.randomUUID() + "_" + fileName;

        // Leggi il file in byte
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        // Convertilo in una stringa Base64
        String base64String = Base64.getEncoder().encodeToString(fileBytes);

        // Carica la stringa Base64 su S3 come testo
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromString(base64String));

        return getFileUrl(key);
    }
 */