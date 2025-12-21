package com.ceres.project.utils.gcp_storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDto {
    private String fileName, url, contentType;
    private long size;
}
