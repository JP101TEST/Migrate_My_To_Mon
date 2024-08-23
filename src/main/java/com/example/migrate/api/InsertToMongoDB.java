package com.example.migrate.api;

import com.example.migrate.service.InsertToMongoDBService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/insert")
public class InsertToMongoDB {

    private final InsertToMongoDBService insertToMongoDBService;

    public InsertToMongoDB(InsertToMongoDBService insertToMongoDBService) {
        this.insertToMongoDBService = insertToMongoDBService;
    }

    @PostMapping("/json")
    public Object insertByJson(@RequestParam("file")MultipartFile json,@RequestParam("collectionName")String collectionName){
        return insertToMongoDBService.insertByJson(json,collectionName);
    }

}
