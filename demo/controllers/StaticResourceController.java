package com.example.demo.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@Slf4j
public class StaticResourceController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String FLASK_URL = "http://localhost:5000";

    @GetMapping("/uploads/**")
    public ResponseEntity<byte[]> serveImage(HttpServletRequest request) {
        try {
            String path = request.getRequestURI();
            String flaskUrl = FLASK_URL + path;


            ResponseEntity<byte[]> response = restTemplate.getForEntity(flaskUrl, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String contentType = "image/png";
                if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(response.getBody());
            }
            return ResponseEntity.notFound().build();

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}