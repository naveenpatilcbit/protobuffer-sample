package org.protobuffsample.demo.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
public class MultipartFileUploadController {

    private static final String MP_UPLOAD_DIR = "mp_uploads";

    @PostMapping(path="/multipart-file-upload")
    public String fileUploadController(@RequestParam("file") MultipartFile file){

        String fileName = file.getOriginalFilename();
        try {
            try {
                if(!Files.exists(Path.of(MP_UPLOAD_DIR))) {
                    Files.createDirectories(Path.of(MP_UPLOAD_DIR));
                }
            } catch (Exception e) {
                    System.out.println(e);
            }

            Files.copy(file.getInputStream(),Path.of(MP_UPLOAD_DIR + "/"+ fileName+System.currentTimeMillis()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "SUCCESS";
    }
}
