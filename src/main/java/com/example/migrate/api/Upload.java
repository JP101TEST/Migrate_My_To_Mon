package com.example.migrate.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class Upload extends ApiAbstract {

    @PostMapping("/mongodb")
    public Object uploadFileToMongoDB(
            @RequestParam("file") MultipartFile file,
            @RequestParam("collection") String collection
    ) {
        return uploadToMongoDBService.upload(file, collection);
    }

    @PostMapping("/mysql")
    public Object uploadFileToMySql(
            @RequestParam("file") MultipartFile file,
            @RequestParam("table") String table
    ) {
        return uploadFileToMySqlService.upload(file, table);
    }


}
