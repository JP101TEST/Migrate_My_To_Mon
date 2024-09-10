package com.example.migrate.service.uploadFile;

import com.example.migrate.dto.request.UploadFileToMySqlRequest;
import com.example.migrate.dto.response.ResponseGeneral;
import com.example.migrate.service.entity.EntityAbstract;
import com.example.migrate.service.entity.EntityGenerate;
import com.example.migrate.service.utility.readFile.csv.ReadCsvFile;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Log4j2
public class UploadFileToMySqlService {
    private UploadFileToMySqlRequest request;
    private String message;
    @Autowired
    private EntityGenerate entityGenerate;

    public Object upload(MultipartFile file, String table) {
        try {
            request = new UploadFileToMySqlRequest(file, table);
            check();
            uploadToDatabase();
            message = "Upload file to " + table + " in MySql successful.";
            return new ResponseGeneral("200", message);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseGeneral("400", e.getMessage());
        }
    }

    private void check() throws Exception {
        request.checkNull();
    }

    private void uploadToDatabase() throws Exception {
        EntityAbstract entity = entityGenerate.getEntity(request.getTableName());
        List<JsonNode> data = ReadCsvFile.readCsvToListJsonNode(request.getFile());
        entity.upload(data);
    }
}
