package com.ceres.project.utils.gcp_storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    private final BucketUtil bucketUtil;

    @Override
    public FileDto uploadFile(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("A file was not specified");
        }
        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new IllegalArgumentException("File's original name is null");
        }
        return bucketUtil.uploadFile(file, fileName);
    }

    @Override
    public FileDto limitFileTypes(MultipartFile submissionFile, List<String> allowedFileTypes) {
        if (submissionFile != null) {
            String fileName = submissionFile.getOriginalFilename();
            if (fileName == null)
                throw new IllegalStateException("File Name is null");

            String[] parts = fileName.split("\\.");
            if (parts.length < 1) {
                throw new IllegalStateException("Invalid file. No file extension found.");
            }

            String extension = parts[parts.length - 1].toLowerCase();
            if (!allowedFileTypes.contains(extension))
                throw new IllegalStateException("Unaccepted file format, " + extension + ". Please upload compressed File only.");

            return uploadFile(submissionFile);

        }
        throw new IllegalStateException("Submission File is Null.");
    }
}
