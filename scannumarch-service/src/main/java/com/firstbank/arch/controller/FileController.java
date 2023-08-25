package com.firstbank.arch.controller;

import com.firstbank.arch.service.FileExtractImgService;
import com.firstbank.arch.service.FileReadQrCodeService;
import com.firstbank.arch.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@RequestMapping("/rest/api/v1/files")
public class FileController {

    FileService fileservice;
    FileExtractImgService extractImageService;
    FileReadQrCodeService readQrCodeService;


    @GetMapping(path = "{folderName}")
    public String sendFileToAlfresco(@PathVariable("folderName") String folderName) {

        return fileservice.pushToAlfresco(folderName);

    }

//    @GetMapping(path = "/images")
//    public Integer extractImages() {
//
//        try {
//            return extractImageService.extractImages();
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//        return 0;
//
//    }

    @GetMapping(path = "/images/process-img")
    public  Integer processImages() {

        try {
            extractImageService.extractImages();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return 0;

    }

//    @GetMapping(path = "/images/qrcode")
//    public  Integer extractQrCode() {
//
//        try {
//            return readQrCodeService.processFile2();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        return 0;
//
//    }


}
