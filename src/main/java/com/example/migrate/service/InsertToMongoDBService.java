package com.example.migrate.service;

import com.example.migrate.dto.response.ResponseGeneral;
import com.example.migrate.exception.CustomException;
import com.example.migrate.service.utility.readFile.json.ReadJsonFile;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Log4j2
public class InsertToMongoDBService extends ServiceAbstract {

    public Object insertByJson(MultipartFile json, String collectionName) {
        try {
            checkMongoDB();
            checkInsertFile(json, "json");
            insertToCollection(MongoClients.create(MONGODB_URL).getDatabase(MONGODB_DATABASE).getCollection(collectionName), json, "json");
        } catch (Exception e) {
            return new ResponseGeneral("400", e.getMessage());
        }
        return new ResponseGeneral("200", "Insert successful.");
    }

    private void checkMongoDB() throws CustomException {
        if (!checkConnectionMongoDB(MONGODB_URL)) {
            log.error("Can't connect MongoDB whit url : " + MONGODB_URL);
            throw new CustomException("Can't connect MongoDB whit url : " + MONGODB_URL);
        }

        if (!checkDatabaseMongoDB(MongoClients.create(MONGODB_URL), MONGODB_DATABASE)) {
            log.error("Can't connect database MongoDB whit database : " + MONGODB_DATABASE);
            throw new CustomException("Can't connect database MongoDB whit database : " + MONGODB_DATABASE);
        }

    }

    private boolean checkConnectionMongoDB(String url) {
        try {
            MongoClient mongoClient = MongoClients.create(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkDatabaseMongoDB(MongoClient mongoClient, String database) {
        try {
            MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void checkInsertFile(MultipartFile file, String fileType) throws CustomException {
        String message = "";
        if (file == null) {
            message = "File " + fileType + " is missing.";
            log.error(message);
            throw new CustomException(message);
        }

        try {
            if (!file.getOriginalFilename().split("\\.")[file.getOriginalFilename().split("\\.").length - 1].equals(fileType)) {
                message = "This file " + file.getOriginalFilename() + " is not " + fileType + " type.";
                log.error(message);
                throw new CustomException(message);
            }
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(e.fillInStackTrace());
            throw new CustomException(e.getMessage());
        }

        if (file.isEmpty()) {
            message = "File " + fileType + " is empty.";
            log.error(message);
            throw new CustomException(message);
        }

    }

    private void insertToCollection(MongoCollection mongoCollection, MultipartFile file, String fileType) throws Exception {
        List<Document> documentList = null;
        try {
            documentList = readFileToListDoc(file, fileType);
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
        mongoCollection.insertMany(documentList);
    }

    private List<Document> readFileToListDoc(MultipartFile file, String fileType) throws Exception {
        switch (fileType) {
            case "json":
                return ReadJsonFile.readJsonToListDoc(file);
            default:
                return null;
        }
    }

}
