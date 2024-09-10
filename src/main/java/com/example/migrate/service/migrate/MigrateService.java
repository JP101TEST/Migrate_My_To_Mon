package com.example.migrate.service.migrate;

import com.example.migrate.dto.response.ResponseGeneral;
import com.example.migrate.exception.CustomException;
import com.example.migrate.service.ServiceAbstract;
import com.example.migrate.service.entity.EntityAbstract;
import com.example.migrate.service.entity.EntityGenerate;
import com.example.migrate.service.utility.check.CheckConnectionDatabase;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class MigrateService extends ServiceAbstract {

    @Value("${migrate.limit.get.row.per.loop}")
    private int limitGetRowDataPerLoop;
    @Autowired
    private EntityGenerate entityGenerate;
    private String message;
    private String tableName;


    public Object migrate(String tableName) {
        try {
            this.tableName = tableName;
            check();
            migrate();
            message = "Migrate this table " + tableName + " successful.";
            return new ResponseGeneral("200", message);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseGeneral("400", e.getMessage());
        }
    }

    private void check() throws Exception {
        if (tableName == null || tableName.isEmpty()) {
            message = "table name is missing.";
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


    private void migrate() throws Exception {
        MongoCollection<Document> collection = MongoClients.create(MONGODB_URL).getDatabase(MONGODB_DATABASE).getCollection(this.tableName);
        EntityAbstract entity = entityGenerate.getEntity(tableName);
        int loopMax = (int) Math.ceil((double) entity.getSize() / 95);
        //System.out.println("Loop max = " + loopMax);
        for (int loop = 0; loop < loopMax; loop++) {
            Pageable pageable = PageRequest.of(loop, 95);
            List<JsonNode> jsonNodes = entity.getListJsonLimit(pageable);
            System.out.println(jsonNodes);
            for (JsonNode row : jsonNodes) {
                Document doc = Document.parse(row.toString());
                Bson filter = Filters.eq("id", doc.getInteger("id"));

                // Check if the document already exists
                if (collection.find(filter).first() == null) {
                    collection.insertOne(doc);
                }
            }
        }
    }

}
