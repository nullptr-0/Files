package com.nullptr.files.controller;

import com.nullptr.files.entity.FileMetadata;
import com.nullptr.files.service.FileStorageService;
import com.nullptr.files.validation.InputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/f")
public class FileOperationController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/ul")
    public CompletableFuture<ResponseEntity<String>> uploadFile(@RequestParam("file") MultipartFile file,
                                                                @RequestParam("title") String title,
                                                                @RequestParam("description") String description) {
        if (!InputValidator.isValid(title) || !InputValidator.isValid(description)) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid input detected"));
        }
        return fileStorageService.save(file, title, description)
                .thenApply(response -> ResponseEntity.status(HttpStatus.OK).body(response))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to upload file"));
    }

    @GetMapping(value = "/dl/{title}", produces = "application/octet-stream")
    public CompletableFuture<ResponseEntity<?>> downloadFile(@PathVariable String title,
                                                              @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {
        if (!InputValidator.isValid(title)) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid input detected"));
        }
        return fileStorageService.loadByTitle(title, rangeHeader);
    }

    @GetMapping("/ls")
    public CompletableFuture<ResponseEntity<List<FileMetadata>>> listAllFiles() {
        return fileStorageService.listAllFiles()
                .thenApply(files -> ResponseEntity.status(HttpStatus.OK).body(files));
    }

    @GetMapping("/dt")
    public CompletableFuture<ResponseEntity<FileMetadata>> getFileDetails(@RequestParam("title") String title) {
        if (!InputValidator.isValid(title)) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null));
        }
        return fileStorageService.getFileDetailsByTitle(title)
                .thenApply(file -> file.map(f -> ResponseEntity.status(HttpStatus.OK).body(f))
                        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)));
    }

    @PostMapping("/fd")
    public CompletableFuture<ResponseEntity<List<String>>> findFiles(@RequestBody FileSearchRequest searchRequest) {
        if (!InputValidator.isValid(searchRequest.getTitle())) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null));
        }
        return fileStorageService.findFiles(searchRequest.getTitle(), searchRequest.getDate())
                .thenApply(titles -> ResponseEntity.status(HttpStatus.OK).body(titles));
    }
}

class FileSearchRequest {
    private String title;
    private LocalDateTime date;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
