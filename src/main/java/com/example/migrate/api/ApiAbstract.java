package com.example.migrate.api;

import com.example.migrate.service.migrate.MigrateService;
import com.example.migrate.service.purgeAndArchives.PurgeAndArchiveService;
import com.example.migrate.service.uploadFile.UploadFileToMongoDBService;
import com.example.migrate.service.uploadFile.UploadFileToMySqlService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ApiAbstract {
    @Autowired
    protected UploadFileToMongoDBService uploadToMongoDBService;
    @Autowired
    protected UploadFileToMySqlService uploadFileToMySqlService;
    @Autowired
    protected MigrateService migrate;
    @Autowired
    protected PurgeAndArchiveService purgeAndArchiveService;
}
