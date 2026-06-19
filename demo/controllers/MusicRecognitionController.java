package com.example.demo.controllers;

import com.example.demo.model.NoteList;
import com.example.demo.model.RecognitionResponse;
import com.example.demo.model.User;
import com.example.demo.repositories.NoteListRepository;
import com.example.demo.services.MusicRecognitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;


@RestController
@RequestMapping("/api/music")
@Slf4j
public class MusicRecognitionController {

    private final MusicRecognitionService recognitionService;
    private final NoteListRepository noteListRepository;
    public MusicRecognitionController(MusicRecognitionService recognitionService, NoteListRepository noteListRepository) {
        this.recognitionService = recognitionService;
        this.noteListRepository = noteListRepository;
    }
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("file", resource);


            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String flaskUrl = "http://localhost:5000/upload";

            ResponseEntity<Map> response = restTemplate.postForEntity(flaskUrl, requestEntity, Map.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }


    @PostMapping("/recognize")
    public ResponseEntity<RecognitionResponse> recognize(
            @RequestParam("file") MultipartFile file) {


        if (!recognitionService.checkPythonServiceHealth()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(createErrorResponse("Python recognition service is not available"));
        }

        if (!isValidImageFile(file)) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid file type. Please upload an image (JPEG, PNG, etc.)"));
        }

        try {
            RecognitionResponse response = recognitionService.recognizeMusicSheet(file);

            return ResponseEntity.ok(response);


        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Recognition failed: " + e.getMessage()));
        }
    }
    @PostMapping("/process")
    public ResponseEntity<?> processImage(@RequestParam("file") MultipartFile file) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String flaskUrl = "http://localhost:5000/recognize";

            ResponseEntity<Map> response = restTemplate.postForEntity(flaskUrl, requestEntity, Map.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Music Recognition API");
        response.put("pythonServiceAvailable", recognitionService.checkPythonServiceHealth());
        response.put("timestamp", new Date());

        return ResponseEntity.ok(response);
    }

    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null &&
                (contentType.startsWith("image/jpeg") ||
                        contentType.startsWith("image/png") ||
                        contentType.startsWith("image/jpg"));
    }

    private RecognitionResponse createErrorResponse(String error) {
        RecognitionResponse response = new RecognitionResponse();

        return response;
    }
}