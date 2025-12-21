package com.ceres.project.utils.gcp_storage;

import com.alibaba.fastjson2.JSONObject;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.internal.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class BucketUtil {

    @Value("${gcp.bucket.name}")
    private String bucketName;

    @Value("${spring.cloud.gcp.config.project-id}")
    private String projectId;

    @Value("classpath:banded-plateau-321516-006939d9d275.json")
    private Resource credentialsJsonFile;

    @Value("${app.name}")
    private String appName;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Storage storage;

    public FileDto uploadFile(MultipartFile file, String fileName) {
        try {
            InputStream inputStream = credentialsJsonFile.getInputStream();
            StorageOptions storageOptions = StorageOptions.newBuilder().setProjectId(projectId)
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .build();

            storage = storageOptions.getService();
            RandomString id = new RandomString(20, ThreadLocalRandom.current());
            Bucket bucket = storage.get(bucketName, Storage.BucketGetOption.fields());
            String gcpDirectoryName = appName + "/" + uploadDir;
            Blob blob = bucket.create(gcpDirectoryName + "/" + id.nextString() + file.getContentType(),
                    file.getInputStream(), file.getContentType());
            if (blob != null) {
                String signedUrl = blob.signUrl(3600, TimeUnit.DAYS).toString();
                return new FileDto(
                        blob.getName(),
                        signedUrl,
                        blob.getContentType(),
                        blob.getSize()
                );
            }
            throw new IllegalStateException("An error occurred while uploading file " + fileName);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public List<JSONObject> listFiles() {
        List<JSONObject> fileNames = new ArrayList<>();
        Bucket bucket = storage.get(bucketName);
        for (Blob blob : bucket.list().iterateAll()) {
            JSONObject o = new JSONObject();
            o.put("fileName", blob.getName());
            o.put("fileSize", blob.getSize());
            o.put("contentType", blob.getContentType());

            if (blob.getName().contains(uploadDir)) {
                fileNames.add(o);
            }
        }
        return fileNames;
    }

    public String readFile(String objectName) {
        Bucket bucket = storage.get(bucketName);
        Blob blob = bucket.get(objectName);
        byte[] content = blob.getContent();
        return new String(content, StandardCharsets.UTF_8);
    }

    public boolean deleteFile(String fileName) {
        Blob blob = storage.get(bucketName, fileName);
        if (blob != null && blob.exists()) {
            return blob.delete();
        }
        return false;
    }
}
