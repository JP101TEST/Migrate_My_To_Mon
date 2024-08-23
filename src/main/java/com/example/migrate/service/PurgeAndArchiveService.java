package com.example.migrate.service;

import com.example.migrate.dto.response.ResponseGeneral;
import com.example.migrate.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class PurgeAndArchiveService {
    private final String MONGODB_URL = "mongodb://root:root@localhost:27017/";
    private final String MONGODB_DATABASE = "datanosql";
    private final String LOCAL_STORE = "local";

    private final ObjectMapper objectMapper;

    public PurgeAndArchiveService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Object purgeAndArchive(String collectionName, String limit, String beforeYear) {
        long start = System.currentTimeMillis();

        try {
            checkInputConfigPAA(collectionName, limit, beforeYear);
        } catch (Exception e) {
            return new ResponseGeneral("400", e.getMessage());
        }

        MongoCollection mongoCollection = MongoClients.create(MONGODB_URL).getDatabase(MONGODB_DATABASE).getCollection(collectionName);
        int maxFileSplitSize = getCountFileToSplit(mongoCollection, limit, beforeYear);
        try {
            for (int fileSplitIndex = 1; fileSplitIndex <= maxFileSplitSize; fileSplitIndex++) {
                // Archive
                List<Document> documentList = getListDocumentFromCollectionByLessYear(mongoCollection, limit, beforeYear);
                removeId(documentList);
                managementArchive(documentList,collectionName,fileSplitIndex ,beforeYear);
                // Purge
                removeDocListInCollection(mongoCollection, documentList);
            }
        }catch (Exception e){
            return new ResponseGeneral("400", e.getMessage());
        }

        long end = System.currentTimeMillis();
        long time = end - start;
        // Convert milliseconds to seconds
        long elapsedSeconds = time / 1000;
        // Calculate hours, minutes, and seconds
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;
        return new ResponseGeneral("200", "Purge and Archive | Successful.| Time = " + hours + " : " + minutes + " : " + seconds + " | ");
    }

    // PAA = Purge And Archive
    private void checkInputConfigPAA(String collectionName, String limit, String beforeYear) throws CustomException {
        if (collectionName.isEmpty()) {
            log.error("Collection name is missing");
            throw new CustomException("Collection name is missing");
        }

        try {
            checkLimitConfig(limit);
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }

        try {
            checkBeforeYearConfig(beforeYear);
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }

        if (!checkConnectionMongoDB(MONGODB_URL)) {
            log.error("Can't connect MongoDB whit url : " + MONGODB_URL);
            throw new CustomException("Can't connect MongoDB whit url : " + MONGODB_URL);
        }

        if (!checkDatabaseMongoDB(MongoClients.create(MONGODB_URL), MONGODB_DATABASE)) {
            log.error("Can't connect database MongoDB whit database : " + MONGODB_DATABASE);
            throw new CustomException("Can't connect database MongoDB whit database : " + MONGODB_DATABASE);
        }

        try {
            checkCollectionDatabase(MongoClients.create(MONGODB_URL).getDatabase(MONGODB_DATABASE), collectionName);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(e.getMessage());
        }

        MongoCollection mongoCollection = MongoClients.create(MONGODB_URL).getDatabase(MONGODB_DATABASE).getCollection(collectionName);
        if (!checkDocumentListSearchByLessYearEmpty(mongoCollection, limit, beforeYear)) {
            log.error("Can't find document by this year " + beforeYear);
            throw new CustomException("Can't find document by this year " + beforeYear);
        }

    }

    private void checkLimitConfig(String limit) throws CustomException {
        try {
            if (limit == null || limit.isEmpty()) {
                log.error("The limit of documents per file is missing.");
                throw new CustomException("The limit of documents per file is missing.");
            }
            if (Long.parseLong(limit) < 1) {
                log.error("The limit of documents per file must be at least 1.");
                throw new CustomException("The limit of documents per file must be at least 1.");
            }
        } catch (NumberFormatException e) {
            log.error("The limit must be a valid number.");
            throw new CustomException("The limit must be a valid number.");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(e.getMessage());
        }
    }

    private void checkBeforeYearConfig(String beforeYear) throws CustomException {
        try {
            if (beforeYear == null || beforeYear.isEmpty()) {
                log.error("The before year is missing.");
                throw new CustomException("The before year is missing.");
            }
            if (Integer.parseInt(beforeYear) < 1000) {
                log.error("The before year format YYYY.");
                throw new CustomException("The before year format YYYY.");
            }
        } catch (NumberFormatException e) {
            log.error("The before year is not in a valid format. Please provide a valid date YYYY.");
            throw new CustomException("The before year is not in a valid format. Please provide a valid date YYYY.");
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(e.getMessage());
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

    private void checkCollectionDatabase(MongoDatabase mongoDatabase, String collectionName) throws CustomException {
        try {
            MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);
            if (mongoCollection.countDocuments() < 1) {
                log.error("Collection {} is empty.", collectionName);
                throw new CustomException("Collection " + collectionName + " is empty.");
            }
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            log.error("Can't connect collection in database MongoDB whit collection : {} .Please check collection in MongoDB", collectionName);
            throw new CustomException("Can't connect collection in database MongoDB whit collection : " + collectionName + " .Please check collection in MongoDB");
        }
    }

    private boolean checkDocumentListSearchByLessYearEmpty(MongoCollection mongoCollection, String limit, String beforeYear) {
        long countDocFromSearch = getListDocCountFromSearchLessYear(mongoCollection, beforeYear);
        int countFileToSplit = (int) Math.ceilDiv(countDocFromSearch, Long.parseLong(limit));
        //System.out.println("countDocFromSearch : "+countDocFromSearch);
        //System.out.println("countFileToSplit : "+countFileToSplit);
        if (countFileToSplit < 1) {
            return false;
        }
        return true;
    }

    private long getListDocCountFromSearchLessYear(MongoCollection mongoCollection, String beforeYear) {
        String date = beforeYear + "-01-01";
        Bson bson = Filters.lte("date", date);
        return mongoCollection.countDocuments(bson);
    }

    private List<Document> getListDocumentFromCollectionByLessYear(MongoCollection mongoCollection, String limit, String beforeYear) {
        List<Document> result = new ArrayList<>();
        Bson filters = Filters.lte("date", beforeYear + "-01-01");
        Bson sort = Sorts.descending("date");
        mongoCollection.find(filters).limit(Integer.parseInt(limit)).sort(sort).into(result);
        return result;
    }

    private int getCountFileToSplit(MongoCollection mongoCollection, String limit, String beforeYear) {
        long countDocFromSearch = getListDocCountFromSearchLessYear(mongoCollection, beforeYear);
        return (int) Math.ceilDiv(countDocFromSearch, Long.parseLong(limit));
    }

    public void testCheckAndPrepareStorageInDay(){
        Path archiveCollectionDirectory = Paths.get(LOCAL_STORE,getDayWhenArchiveDirectory(),"collectionName");
        checkAndPrepareStorageLocation(archiveCollectionDirectory);
    }

    private void removeId(List<Document> documentList){
        for (Document document:documentList){
            document.remove("_id");
        }
    }
    private void managementArchive(List<Document> documentList,String collectionName,int fileSplitIndex,String lessYer) throws Exception{
        Path archiveCollectionDirectory = Paths.get(LOCAL_STORE,getDayWhenArchiveDirectory(),collectionName);
        checkAndPrepareStorageLocation(archiveCollectionDirectory);
        archiveToFileJson(documentList,archiveCollectionDirectory,fileSplitIndex,lessYer);

    }

    private void checkAndPrepareStorageLocation( Path archiveCollectionDirectory) {
        String[] directoryStore = archiveCollectionDirectory.toString().split("\\\\");
        if (!Path.of(directoryStore[0]).toFile().exists()){
            Path.of(directoryStore[0]).toFile().mkdir();
        }
        if (!Path.of(directoryStore[0],directoryStore[1]).toFile().exists()){
            Path.of(directoryStore[0],directoryStore[1]).toFile().mkdir();
        }
        if (!archiveCollectionDirectory.toFile().exists()){
            archiveCollectionDirectory.toFile().mkdir();
        }
    }


    private String getDayWhenArchiveDirectory(){
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")).toString();
    }

    private void archiveToFileJson(List<Document> documentList,Path archiveCollectionDirectory,int fileSplitIndex,String lessYear)throws CustomException {
        String nameFile = archiveCollectionDirectory.toString().split("\\\\")[2] + "_"+lessYear+"_"+fileSplitIndex+".json";
        Path fileToCreate = Paths.get(archiveCollectionDirectory.toString(),nameFile);
        try {
            Files.write(fileToCreate.toAbsolutePath(),objectMapper.writeValueAsBytes(documentList));
        }catch (Exception e){
            log.error(e.getMessage());
            throw new CustomException(e.getMessage());
        }
    }

    private void removeDocListInCollection(MongoCollection mongoCollection, List<Document> documentList) {
        List<Bson> bsonList = new ArrayList<>();
        for (Document document : documentList) {
            bsonList.add(document);
        }
        Bson query = Filters.or(bsonList);
        mongoCollection.deleteMany(query);
    }
}
