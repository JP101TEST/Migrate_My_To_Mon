package com.example.migrate.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/purge_Archive")
public class PurgeAndArchive extends ApiAbstract {

    @PostMapping("")
    public Object purgeAndArchive(@RequestParam("collection") String collectionName,  @RequestParam("year") String yearSelect) {
        return  purgeAndArchiveService.purgeAndArchive(collectionName, yearSelect);
    }

}
