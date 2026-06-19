package com.example.demo.controllers;

import com.example.demo.model.Note;
import com.example.demo.model.NoteList;
import com.example.demo.model.User;
import com.example.demo.repositories.NoteListRepository;
import com.example.demo.repositories.NoteRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/flask")
@Slf4j
public class FlaskProxyController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final NoteListRepository noteListRepository;
    private final NoteRepository noteRepository;
    private final String FLASK_URL = "http://localhost:5000";

    public FlaskProxyController(NoteListRepository noteListRepository, NoteRepository noteRepository) {
        this.noteListRepository = noteListRepository;
        this.noteRepository = noteRepository;
    }

    @GetMapping("/note/{id}")
    @ResponseBody
    public ResponseEntity<?> getNoteById(@PathVariable Long id) {
        try {
            NoteList note = noteListRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Note not found"));

            String base64Image = java.util.Base64.getEncoder().encodeToString(note.getImage());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", note.getId(),
                    "name", note.getName() != null ? note.getName() : "",
                    "composer", note.getComposer() != null ? note.getComposer() : "",
                    "image_base64", "data:image/png;base64," + base64Image,
                    "image_path", "/uploads/" + note.getId()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    @PostMapping("/addNoteInfo/{id}")
    public ResponseEntity<?> addNoteInfo(@RequestBody Map<String, Object> request, @PathVariable Long id) {
        try {
            String noteName = (String) request.get("note_name");
            Integer octave = (Integer) request.get("octave");

            NoteList noteList = noteListRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("NoteList not found with id: " + id));

            Note newNote = new Note(noteName, octave);

            noteList.addNote(newNote);

            noteListRepository.save(noteList);

            System.out.println("Добавлена нота к листу ID: " + id);
            System.out.println("Все ноты листа: " + noteList.getNotes());

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    @PutMapping("/list/{id}")
    public ResponseEntity<?> updateNoteMetadata(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String composer) {

        try {
            NoteList note = noteListRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Note not found"));

            note.setName(name);
            note.setComposer(composer);

            noteListRepository.save(note);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Metadata updated"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, HttpSession session) {
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
            User currentUser = (User) session.getAttribute("currentUser");

            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "error", "User not logged in"));
            }
            body.add("file", resource);
            NoteList nlist = new NoteList(resource.getByteArray());
            nlist.setCreatedBy(currentUser);
            NoteList saved = noteListRepository.save(nlist);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> flaskResponse = restTemplate.postForEntity(
                    FLASK_URL + "/upload", requestEntity, Map.class);

            Map flaskBody = flaskResponse.getBody();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("id", saved.getId());
            responseBody.put("image_path", flaskBody.get("image_path"));
            responseBody.put("flask_response", flaskBody);

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/uploads/**")
    public ResponseEntity<byte[]> serveImage(HttpServletRequest request) {
        try {
            String path = request.getRequestURI();
            RestTemplate restTemplate = new RestTemplate();
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
            System.out.println("Error serving image: {}");
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/recognize")
    public ResponseEntity<?> proxyRecognize(@RequestBody Map<String, Object> request) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    FLASK_URL + "/recognize", request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/recognize-selection")
    public ResponseEntity<?> proxyRecognizeSelection(@RequestBody Map<String, Object> request) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    FLASK_URL + "/recognize-selection", request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> proxyHealth() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    FLASK_URL + "/health", Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "error", e.getMessage()));
        }
    }
}