package com.example.demo.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import com.example.demo.model.RecognitionResponse;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;



@Service
@Slf4j
public class MusicRecognitionService {
    @Value("${python.api.url}")
    private String pythonApiUrl;

    private final RestTemplate restTemplate;

    @Autowired
    public MusicRecognitionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RecognitionResponse recognizeMusicSheet(MultipartFile file) {
        System.out.println("Processing music sheet");

        try {
            org.springframework.http.HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            body.add("file", new MultipartFileResource(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            ));

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            String url = pythonApiUrl + "/recognize";

            System.out.println("Sending request to Python API");

            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    String.class
            );


            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();

                ObjectMapper objectMapper = new ObjectMapper();
                RecognitionResponse result = objectMapper.readValue(
                        responseBody,
                        RecognitionResponse.class
                );

                return result;

            }
            else {
                throw new RuntimeException("Python API returned status: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException e) {

            throw new RuntimeException("HTTP error: " + e.getMessage());
        } catch (HttpServerErrorException e) {

            throw new RuntimeException("Server error: " + e.getMessage());
        } catch (Exception e) {

            throw new RuntimeException("Recognition failed: " + e.getMessage());
        }
    }

    public boolean checkPythonServiceHealth() {
        try {
            String url = pythonApiUrl + "/health";

            ResponseEntity<String> response = restTemplate.getForEntity(
                    url,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.has("status") && "ok".equals(root.get("status").asText());
            }
            return false;

        } catch (Exception e) {
            System.out.println("Python service is not available: {}");
            return false;
        }
    }

    private static class MultipartFileResource extends ByteArrayResource {
        private final String filename;
        private final String contentType;

        public MultipartFileResource(String filename, String contentType, byte[] bytes) {
            super(bytes);
            this.filename = filename;
            this.contentType = contentType;
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public String getDescription() {
            return "MultipartFile: " + filename;
        }
    }
}
