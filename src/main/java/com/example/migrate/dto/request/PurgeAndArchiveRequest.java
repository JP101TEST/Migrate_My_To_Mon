package com.example.migrate.dto.request;

import com.example.migrate.exception.CustomException;
import lombok.Data;

@Data
public class PurgeAndArchiveRequest {
    private String collectionName;
    private String yearSelect;

    public PurgeAndArchiveRequest(String collectionName, String yearSelect) {
        this.collectionName = collectionName;
        this.yearSelect = yearSelect;
    }

    public void checkNull() throws CustomException {
        if (this.collectionName == null || this.collectionName.isEmpty()) throw new CustomException("collectionName is missing.");
        if (this.yearSelect == null || this.yearSelect.isEmpty()) throw new CustomException("yearSelect is missing.");
        if(this.yearSelect.length() != 4)  throw new CustomException("yearSelect format is yyyy.");
    }
}
