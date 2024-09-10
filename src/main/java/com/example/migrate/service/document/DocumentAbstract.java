package com.example.migrate.service.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;

import java.util.List;

public abstract class DocumentAbstract {
    protected ObjectMapper objectMapper = new ObjectMapper();

    public abstract List<Document> getDocListAll() throws Exception;

    public abstract List<Document> getDocListInYear(String year) throws Exception;

    public abstract List<Document> getDocListInYearLimit(String year, int limit) throws Exception;
}
