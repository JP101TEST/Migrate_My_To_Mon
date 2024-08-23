package com.example.migrate.api;

import com.example.migrate.service.PurgeAndArchiveService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/purge_Archive")
public class PurgeAndArchive {
    private final PurgeAndArchiveService purgeAndArchiveService;

    public PurgeAndArchive(PurgeAndArchiveService purgeAndArchiveService) {
        this.purgeAndArchiveService = purgeAndArchiveService;
    }

    @PostMapping("")
    public Object purgeAndArchive(@RequestParam("collection") String collectionName,@RequestParam("limit") String limit,@RequestParam("beforeYear") String beforeYear) {
        return purgeAndArchiveService.purgeAndArchive(collectionName,limit,beforeYear);
    }


    @PostMapping("/test")
    public void test(){
        purgeAndArchiveService.testCheckAndPrepareStorageInDay();
    }
}
