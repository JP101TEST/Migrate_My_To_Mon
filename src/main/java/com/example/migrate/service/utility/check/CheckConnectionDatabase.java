package com.example.migrate.service.utility.check;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class CheckConnectionDatabase {

    public static boolean checkConnectionMongoDB(String url) {
        try {
            MongoClient mongoClient = MongoClients.create(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkDatabaseMongoDB(MongoClient mongoClient, String database) {
        try {
            MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkCollectionDatabaseMongoDB(MongoClient mongoClient, String database,String collection) {
        try {
            MongoCollection<Document> mongoDatabase = mongoClient.getDatabase(database).getCollection(collection);
            return mongoDatabase.countDocuments() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static Long getCountFromListInYear(MongoCollection<Document> collection,String year) {
        return collection.countDocuments(Filters.and(
                Filters.gte("release", year + "-01-01 00:00:00.0"),
                Filters.lte("release", year + "-12-31 23:59:59.9")));
    }
}
