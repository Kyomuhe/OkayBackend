package com.ceres.project.utils.gcp_storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    FileDto uploadFile(MultipartFile file);

    FileDto limitFileTypes(MultipartFile submissionFile, List<String> allowedFileTypes);
}
