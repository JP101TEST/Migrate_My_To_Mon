package com.example.migrate.service.document;

import com.example.migrate.exception.CustomException;
import com.example.migrate.service.ServiceAbstract;
import com.example.migrate.service.entity.E_Car;
import com.example.migrate.service.entity.E_TechCompanies;
import com.example.migrate.service.entity.EntityAbstract;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentGenerate extends ServiceAbstract {
    public DocumentAbstract getDocument(String document, MongoCollection<Document> collection) throws Exception {
        return switch (document) {
            case "car" -> new Doc_Car(collection);
            default -> throw new CustomException("Not document " + document + " in mongodb.");
        };
    }
}
