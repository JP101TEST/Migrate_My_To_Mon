package com.example.migrate.dto.request;

import com.example.migrate.exception.CustomException;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadFileToMySqlRequest {
    private MultipartFile file;
    private String tableName;

    public UploadFileToMySqlRequest(MultipartFile file, String tableName) {
        this.file = file;
        this.tableName = tableName;
    }

    public void checkNull() throws CustomException {
        if (this.file == null || this.file.isEmpty()) throw new CustomException("file is missing.");
        if (this.tableName == null || this.tableName.isEmpty()) throw new CustomException("tableName is missing.");
    }
}
