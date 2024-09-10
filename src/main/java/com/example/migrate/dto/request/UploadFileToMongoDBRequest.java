package com.example.migrate.dto.request;

import com.example.migrate.exception.CustomException;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadFileToMongoDBRequest {
    private MultipartFile file;
    private String collection;

    public UploadFileToMongoDBRequest(MultipartFile file, String collection) {
        this.file = file;
        this.collection = collection;
    }

    public void checkNull() throws CustomException {
        if (this.file == null || this.file.isEmpty()) throw new CustomException("file is missing.");
        if (this.collection == null || this.collection.isEmpty()) throw new CustomException("collection is missing.");
    }
}
