package com.example.migrate.api;

import com.example.migrate.service.UploadService;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@Log4j2
public class Upload {
    private final UploadService uploadService;

    public Upload(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("")
    public Object upload(
            @RequestParam("file") MultipartFile file
    ) {
        System.out.println(uploadService.getClass().getName());
        return uploadService.upload(file);
    }

}
