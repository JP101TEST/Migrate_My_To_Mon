package com.example.migrate.service.uploadFile;

import com.example.migrate.dto.request.UploadFileToMongoDBRequest;
import com.example.migrate.dto.response.ResponseGeneral;
import com.example.migrate.exception.CustomException;
import com.example.migrate.service.ServiceAbstract;
import com.example.migrate.service.utility.check.CheckConnectionDatabase;
import com.example.migrate.service.utility.readFile.json.ReadJsonFile;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@Log4j2
public class UploadFileToMongoDBService  extends ServiceAbstract {

    private UploadFileToMongoDBRequest request;
    private String message;


    public Object upload(MultipartFile file, String collection) {
        try {
            request = new UploadFileToMongoDBRequest(file, collection);
            check();
            uploadToDatabase();
            message = "Upload " + file.getOriginalFilename() + " to " + collection + " in " + MONGODB_DATABASE + " successful.";
            log.info(message);
            return new ResponseGeneral("200", message);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseGeneral("400", e.getMessage());
        }
    }

    private void check() throws Exception {
        request.checkNull();

        if (!Objects.equals(request.getCollection(), MONGODB_COLLECTION_TEST_YEAR)) {
            message = "Collection is invalid.";
            log.error(message);
            throw new CustomException(message);
        }

        if (!request.getFile().getOriginalFilename().split("\\.")[request.getFile().getOriginalFilename().split("\\.").length - 1].equals("json")) {
            message = "File upload is not json.";
            log.error(message);
            throw new CustomException(message);
        }

        if (!CheckConnectionDatabase.checkConnectionMongoDB(MONGODB_URL)) {
            message = "Can't connect MongoDB whit url : " + MONGODB_URL;
            log.error(message);
            throw new CustomException(message);
        }

        if (!CheckConnectionDatabase.checkDatabaseMongoDB(MongoClients.create(MONGODB_URL), MONGODB_DATABASE)) {
            message = "Can't connect database MongoDB whit database : " + MONGODB_DATABASE;
            log.error(message);
            throw new CustomException(message);
        }

    }

    private void uploadToDatabase() throws Exception {
        MongoCollection<Document> mongoCollection = MongoClients.create(MONGODB_URL).getDatabase(MONGODB_DATABASE).getCollection(MONGODB_COLLECTION_TEST_YEAR);
        List<Document> documentList = ReadJsonFile.readJsonToListDoc(request.getFile());
        mongoCollection.insertMany(documentList);
    }
}
