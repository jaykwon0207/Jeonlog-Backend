package com.jeonlog.exhibition_recommender.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public String health() {
        return "OKfd332";
    }

    @GetMapping("/")
    public String root() {
        return "running";
    }

}