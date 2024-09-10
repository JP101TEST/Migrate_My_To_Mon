package com.example.migrate.service.ole;

import com.example.migrate.dto.response.ResponseGeneral;
import com.example.migrate.entity.Car;
import com.example.migrate.entity.TechCompanies;
import com.example.migrate.entity.Test;
import com.example.migrate.exception.CustomException;
import com.example.migrate.service.ServiceAbstract;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.persistence.Table;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Log4j2
public class MigrateServiceOle extends ServiceAbstract {
    private final Set<String> validTableNames;
    private int maxRow;

    public MigrateServiceOle() {
        this.validTableNames = new HashSet<>();
        validTableNames.add("car");
        validTableNames.add("tech_companies");
        validTableNames.add("test");
    }

    /**
     * Gets the table name associated with the provided entity class.
     * <br>
     * ดึงชื่อของตารางที่เชื่อมโยงกับคลาสเอนทิตีที่ให้มา
     *
     * @param entityClass The entity class from which to extract the table name.
     *                    คลาสเอนทิตีที่ต้องการดึงชื่อของตาราง
     * @return The name of the table associated with the entity class, or empty if no table annotation is present.
     * ชื่อของตารางที่เชื่อมโยงกับคลาสเอนทิตี หรือค่าว่างหากไม่มีการกำหนด @Table
     */
    private static String getTableName(Class<?> entityClass) {
        // Check if the @Table annotation is present
        // ตรวจสอบว่ามี @Table annotation หรือไม่
        if (entityClass.isAnnotationPresent(Table.class)) {
            // Get the @Table annotation
            // ดึงข้อมูล @Table annotation
            Table table = entityClass.getAnnotation(Table.class);
            return table.name();
        }
        // If @Table annotation is not present, return empty
        return "";
    }

    public Object migrate(String tableName, int limit) {

        if (limit < 1) {
            log.error("Migrate | Limit less is 1");
            return new ResponseGeneral("400", "Migrate | Limit less is 1");
        }

        maxRow = limit;
        System.out.println(maxRow);
        long start = System.currentTimeMillis();
        // Connect to MongoDB
        MongoClient mongoClient = MongoClients.create(MONGODB_URL);
        // Get the database
        MongoDatabase database = mongoClient.getDatabase(MONGODB_DATABASE);
        // Get the collection
        MongoCollection<Document> collection = null;

        if (validTableNames.contains(tableName)) {
            collection = database.getCollection(tableName);
        } else {
            log.error("Migrate | Unknown table name: " + tableName);
            return new ResponseGeneral("400", "Migrate | Unknown table name: " + tableName);
        }

        int maxLoop = 0;
        switch (tableName) {
            case "car":
                if (carRepository.count() < 1) {
                    log.error("Migrate | Table | " + tableName + " | is empty.");
                    return new ResponseGeneral("400", "Migrate | Table | " + tableName + " | is empty.");
                }
                if (maxRow > carRepository.count()) {
                    log.error("Migrate | Limit | " + limit + " | is high than table have.");
                    return new ResponseGeneral("400", "Migrate | Limit | " + limit + " | is high than table have.");
                }
                maxLoop = (int) Math.ceil(((double) carRepository.count()) / maxRow);

                break;
            case "tech_companies":
                if (techCompaniesRepository.count() < 1) {
                    log.error("Migrate | Table | " + tableName + " | is empty.");
                    return new ResponseGeneral("400", "Migrate | Table | " + tableName + " | is empty.");
                }
                if (maxRow > techCompaniesRepository.count()) {
                    log.error("Migrate | Limit | " + limit + " | is high than table have.");
                    return new ResponseGeneral("400", "Migrate | Limit | " + limit + " | is high than table have.");
                }
                maxLoop = (int) Math.ceil(((double) techCompaniesRepository.count()) / maxRow);
                break;
            case "test":
                if (testRepository.count() < 1) {
                    log.error("Migrate | Table | " + tableName + " | is empty.");
                    return new ResponseGeneral("400", "Migrate | Table | " + tableName + " | is empty.");
                }
                if (maxRow > testRepository.count()) {
                    log.error("Migrate | Limit | " + limit + " | is high than table have.");
                    return new ResponseGeneral("400", "Migrate | Limit | " + limit + " | is high than table have.");
                }
                maxLoop = (int) Math.ceil(((double) testRepository.count()) / maxRow);
                break;
        }

        // Loop
        System.out.println("Max loop : " + maxLoop);
        for (int index = 0; index < maxLoop; index++) {
            Pageable pageable = PageRequest.of(index, maxRow);

//        System.out.println("Size row : " + testRepository.count() + " | Max row : " + maxRow + " | Count loop : " + Math.ceil(testRepository.count() / maxRow));
//        Pageable pageables = PageRequest.of(1, maxRow);
//        List<Test> testLimit = testRepository.findAll(pageables).getContent();
//        System.out.println("Test limit : " + testLimit.size());

            List<JsonNode> sqlData = null;

            try {
                sqlData = getListSql(tableName, pageable);
            } catch (Exception e) {
                return new ResponseGeneral("400", e.getMessage());
            }
            System.out.println("Index | " + index + " | " + sqlData.size());
            for (JsonNode data : sqlData) {
                Document doc = Document.parse(data.toString());

                if (tableName.equals("test")) {
                    collection.insertOne(doc);
                } else {
                    // Make doc for find doc in collection
                    // By new Document( key , value)
                    Document docFind = null;
                    try {
                        docFind = getQuery(doc, tableName);
                    } catch (Exception e) {
                        return new ResponseGeneral("400", e.getMessage());
                    }

                    Document findDocument = collection.find(docFind).first();

                    if (findDocument == null) {
                        collection.insertOne(doc);
                    }
                }
                //System.out.println(doc);
            }
        }
        long end = System.currentTimeMillis();
        long time = end - start;
        // Convert milliseconds to seconds
        long elapsedSeconds = time / 1000;

        // Calculate hours, minutes, and seconds
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;

        log.info("Migrate table | " + tableName + " | to MongoDB successful. | Time : " + time);
        return new ResponseGeneral("200", "Migrate table | " + tableName + " | to MongoDB successful. | Time = " + hours + " : " + minutes + " : " + seconds);
    }

    private List<JsonNode> getListSql(String tableName, Pageable pageable) throws CustomException {
        List<JsonNode> result = new ArrayList<>();
        switch (tableName) {
            case "car":
                List<Car> carList = carRepository.findAll(pageable).getContent();
                for (Car car : carList) {
                    result.add(objectMapper.convertValue(car, JsonNode.class));
                }
                break;
            case "tech_companies":
                List<TechCompanies> techCompaniesList = techCompaniesRepository.findAll(pageable).getContent();
                for (TechCompanies techCompanies : techCompaniesList) {
                    result.add(objectMapper.convertValue(techCompanies, JsonNode.class));
                }
                break;
            case "test":

                List<Test> test = testRepository.findAll(pageable).getContent();
                for (Test te : test) {
                    result.add(objectMapper.convertValue(te, JsonNode.class));
                }
                break;
        }
        if (result.size() > 0) {
            return result;
        } else {
            log.error("Migrate | Error processing data from table | " + tableName + " |.");
            throw new CustomException("Migrate | Error processing data from table | " + tableName + " |.");
        }
    }

    private Document getQuery(Document data, String tableName) throws CustomException {
        switch (tableName) {
            case "car":
                return new Document("modelName", data.get("modelName").toString());
            case "tech_companies":
                return new Document("company", data.get("company").toString());
            default:
                log.error("Migrate | Unknown table name: " + tableName);
                throw new CustomException("Migrate | Unknown table name: " + tableName);
        }
    }
}
