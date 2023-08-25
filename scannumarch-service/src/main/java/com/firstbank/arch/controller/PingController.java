package com.firstbank.arch.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/api/v1")
public class PingController {

    @Value("${application.drive.location}")
    private String drive;


    @GetMapping(path = "/service")
    public String ping() {

        return "ALIVE-"+drive;

    }

}
