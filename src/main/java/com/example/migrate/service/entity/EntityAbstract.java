package com.example.migrate.service.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Pageable;

import java.util.List;

public abstract class EntityAbstract {
    protected ObjectMapper objectMapper = new ObjectMapper();
    public abstract void upload(List<JsonNode> data) throws Exception;
    public abstract List<JsonNode> getListJson();
    public abstract List<JsonNode> getListJsonLimit(Pageable pageable);
    public abstract long getSize();
}
