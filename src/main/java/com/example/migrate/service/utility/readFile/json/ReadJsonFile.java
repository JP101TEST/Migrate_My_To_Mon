package com.example.migrate.service.utility.readFile.json;


import com.example.migrate.exception.CustomException;
import com.example.migrate.service.utility.readFile.ReadFile;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class ReadJsonFile extends ReadFile {

    public static List<Document> readJsonToListDoc(MultipartFile file) throws Exception {
        String message;
        JsonNode jsonNode;
        List<Document> result = new ArrayList<>();
        try {
            jsonNode =objectMapper.readTree(file.getInputStream());
            if (jsonNode == null || jsonNode.isEmpty()) {
                message = "Can't read file " + file.getOriginalFilename() + ".";
                log.error(message);
                throw new CustomException(message);
            }
            for (JsonNode child : jsonNode) {
                result.add(objectMapper.convertValue(child, Document.class));
            }
            if (result.isEmpty()) {
                message = "Can read file but convert to list document is empty.";
                log.error(message);
                throw new CustomException(message);
            }
        } catch (CustomException e) {
            log.error(e.getMessage());
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
        return result;
    }

}
