package com.example.migrate.service.document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class Doc_Car extends DocumentAbstract {
    private MongoCollection<Document> collection;

    public Doc_Car(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    @Override
    public List<Document> getDocListAll() throws Exception {
        List<Document> documentList = new ArrayList<>();
        collection.find().into(documentList);
        return documentList;
    }

    @Override
    public List<Document> getDocListInYear(String year) throws Exception {
        List<Document> documentList = new ArrayList<>();
        collection.find().filter(Filters.and(
                Filters.gte("release", year + "-01-01 00:00:00.0"),
                Filters.lte("release", year + "-12-31 23:59:59.9"))).into(documentList);
        return documentList;
    }

    @Override
    public List<Document> getDocListInYearLimit(String year, int limit) throws Exception {
        List<Document> documentList = new ArrayList<>();
        collection.find().filter(Filters.and(
                Filters.gte("release", year + "-01-01 00:00:00.0"),
                Filters.lte("release", year + "-12-31 23:59:59.9"))).limit(limit).into(documentList);
        return documentList;
    }


}
