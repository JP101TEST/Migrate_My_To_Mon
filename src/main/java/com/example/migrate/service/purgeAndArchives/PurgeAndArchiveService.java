package com.example.migrate.service.purgeAndArchives;

import com.example.migrate.dto.request.PurgeAndArchiveRequest;
import com.example.migrate.dto.response.ResponseGeneral;
import com.example.migrate.exception.CustomException;
import com.example.migrate.service.ServiceAbstract;
import com.example.migrate.service.document.DocumentAbstract;
import com.example.migrate.service.document.DocumentGenerate;
import com.example.migrate.service.utility.check.CheckConnectionDatabase;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.opencsv.CSVWriter;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class PurgeAndArchiveService extends ServiceAbstract {
    @Value("${mongodb.location.purgeAndArchive}")
    private String locationDirectoryPurgeAndArchive;
    @Autowired
    private DocumentGenerate documentGenerate;
    private PurgeAndArchiveRequest request;
    private String message;

    public Object purgeAndArchive(String collectionName, String yearSelect) {
        try {
            request = new PurgeAndArchiveRequest(collectionName, yearSelect);
            locationDirectoryPurgeAndArchive += "/" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            check();
            purgeAndArchive();
            message = "Purge and archive this collection " + collectionName + " successful.";
            return new ResponseGeneral("200", message);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseGeneral("400", e.getMessage());
        }
//        Path path = Paths.get("localTest");
//        path.toFile().mkdir();
//        Path file = Paths.get(path.toString(), "file.csv");
//        try {
//            FileWriter fileWriter = new FileWriter(file.toFile());
//            CSVWriter csvWriter = new CSVWriter(fileWriter);
//            MongoCollection<Document> collection = MongoClients.create(MONGODB_URL).getDatabase(MONGODB_DATABASE).getCollection("car");
//            List<Document> documentList = new ArrayList<>();
//            collection.find().sort(Sorts.descending("release")).filter(Filters.and(
//                    Filters.gte("release", "2000-01-01 00:00:00.0"),
//                    Filters.lte("release", "2000-12-31 23:59:59.9"))).into(documentList);
//            System.out.println(documentList.getFirst());
//            System.out.println(documentList.getLast());
//            List<String[]> strings = new ArrayList<>();
//            documentList.get(0).remove("_id");
//            //System.out.println(documentList.get(0));
//            //System.out.println(documentList.get(0).size());
//            //System.out.println(documentList.get(0).keySet());
//            Set<String> keySet = documentList.get(0).keySet();
//            String[] header = keySet.toArray(new String[0]);
//            strings.add(header);
//            for (Document document : documentList) {
//                document.remove("_id");
//                System.out.println(document);
//                List<String> stringList = new ArrayList<>();
//                document.entrySet().forEach(stringObjectEntry -> {
//                    stringList.add(stringObjectEntry.getValue().toString());
//                });
//                strings.add(stringList.toArray(new String[0]));
//            }
//            csvWriter.writeAll(strings);
//            csvWriter.close();
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//
//        System.out.println("Create done.");
    }

    private void check() throws Exception {
        request.checkNull();

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

        if (!CheckConnectionDatabase.checkCollectionDatabaseMongoDB(MongoClients.create(MONGODB_URL), MONGODB_DATABASE, request.getCollectionName())) {
            message = "Can't connect find data in collection " + request.getCollectionName() + " from database MongoDB whit database : " + MONGODB_DATABASE;
            log.error(message);
            throw new CustomException(message);
        }

        if (CheckConnectionDatabase.getCountFromListInYear(MongoClients.create(MONGODB_URL).getDatabase(MONGODB_DATABASE).getCollection(request.getCollectionName()),request.getYearSelect()) < 1){
            message = "This year "+request.getYearSelect()+" is empty.";
            log.error(message);
            throw new CustomException(message);
        }

        checkDirectoryAndPrepare(locationDirectoryPurgeAndArchive);
    }

    private void checkDirectoryAndPrepare(String directory) {
        String[] patches = directory.split("/").clone();
        String patchForStore = "";
        for (int index = 0; index < patches.length; index++) {
            //System.out.println(patches[index]);
            patchForStore += patches[index];
            Path newDirectory = Paths.get(patchForStore);
            if (!newDirectory.toFile().exists()) {
                newDirectory.toFile().mkdir();
            }
            if (index < patches.length - 1) {
                patchForStore += "/";
            }
        }
    }

    private void purgeAndArchive() throws Exception {
        MongoCollection<Document> collection = MongoClients.create(MONGODB_URL).getDatabase(MONGODB_DATABASE).getCollection(request.getCollectionName());
        DocumentAbstract document = documentGenerate.getDocument(request.getCollectionName(), collection);
        List<Document> documentList = document.getDocListInYear(request.getYearSelect());
        Path path = Paths.get(locationDirectoryPurgeAndArchive, request.getYearSelect());
        Path pathCsv = Paths.get(path.toString(), request.getYearSelect() + ".csv");
        Path pathJson = Paths.get(path.toString(), request.getYearSelect() + ".json");
        path.toFile().mkdir();
        FileWriter fileWriter = new FileWriter(pathCsv.toFile());
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        List<String[]> strings = new ArrayList<>();
        for (int index = 0; index < documentList.size(); index++) {
            documentList.get(index).remove("_id");
            if (index == 0) {
                String[] header = documentList.get(index).keySet().toArray(new String[0]);
                strings.add(header);
            }
            List<String> stringList = new ArrayList<>();
            documentList.get(index).entrySet().forEach(stringObjectEntry -> {
                stringList.add(stringObjectEntry.getValue().toString());
            });
            strings.add(stringList.toArray(new String[0]));
        }
        csvWriter.writeAll(strings);
        csvWriter.close();
        Files.write(pathJson, objectMapper.writeValueAsBytes(documentList));

        for (Document documentToDelete : documentList) {
            Bson query = Filters.eq("id", documentToDelete.get("id"));
            collection.deleteOne(query);
        }
    }
}


/**
 * {$and:[{"release":{$gte:"2017-01-01 00:00:00.0"}},{"release":{$lte:"201712-31 23:59:59.9"}}]}
 * */