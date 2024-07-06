package com.nullptr.files.service;

import com.nullptr.files.config.FileStorageConfig;
import com.nullptr.files.entity.FileMetadata;
import com.nullptr.files.repository.FileMetadataRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class FileStorageService {

    private final Path root;
    private final String filePolicy;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.root = Paths.get(fileStorageConfig.getUploadDir());
        this.filePolicy = fileStorageConfig.getPolicy();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if ("discard".equalsIgnoreCase(filePolicy)) {
            try {
                Files.walk(root)
                        .sorted((path1, path2) -> path2.compareTo(path1))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException("Could not delete file: " + path, e);
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException("Could not clean up files during service destruction", e);
            }
        }
    }

    @Async("fileOperationTaskExecutor")
    public CompletableFuture<String> save(MultipartFile file, String title, String description) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));

            FileMetadata metadata = new FileMetadata();
            metadata.setFilename(file.getOriginalFilename());
            metadata.setTitle(title);
            metadata.setDescription(description);
            metadata.setUploadTime(LocalDateTime.now());
            fileMetadataRepository.save(metadata);

            return CompletableFuture.completedFuture("File uploaded successfully");
        } catch (Exception e) {
            return CompletableFuture.completedFuture("Failed to upload file");
        }
    }

    @Async("fileOperationTaskExecutor")
    public CompletableFuture<ResponseEntity<?>> loadByTitle(String title, String rangeHeader) throws IOException {
        Optional<FileMetadata> fileMetadataOpt = fileMetadataRepository.findByTitle(title);
        if (fileMetadataOpt.isPresent()) {
            FileMetadata fileMetadata = fileMetadataOpt.get();
            Path file = root.resolve(fileMetadata.getFilename());
            if (Files.exists(file)) {
                long fileLength = Files.size(file);
                if (rangeHeader == null) {
                InputStreamResource resource = new InputStreamResource(Files.newInputStream(file));
                    return CompletableFuture.completedFuture(ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName().toString() + "\"")
                            .body(resource));
                } else {
                    List<HttpRange> httpRanges = HttpRange.parseRanges(rangeHeader);
                    HttpRange httpRange = httpRanges.get(0);
                    long start = httpRange.getRangeStart(fileLength);
                    long end = httpRange.getRangeEnd(fileLength);
                    long rangeLength = end - start + 1;

                    byte[] data = new byte[(int) rangeLength];
                    try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
                        raf.seek(start);
                        raf.readFully(data);
                    }

                    return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName().toString() + "\"")
                            .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                            .body(data));
                }
            }
        }
        return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found"));
    }

    @Async("fileOperationTaskExecutor")
    public CompletableFuture<List<FileMetadata>> listAllFiles() {
        return CompletableFuture.completedFuture(fileMetadataRepository.findAll());
    }

    @Async("fileOperationTaskExecutor")
    public CompletableFuture<Optional<FileMetadata>> getFileDetailsByTitle(String title) {
        return CompletableFuture.completedFuture(fileMetadataRepository.findByTitle(title));
    }

    @Async("fileOperationTaskExecutor")
    public CompletableFuture<List<String>> findFiles(String title, LocalDateTime date) {
        LocalDateTime datePlusOneDay = (date != null) ? date.plusDays(1) : null;
        List<FileMetadata> files = fileMetadataRepository.findByTitleAndDate(title, date, datePlusOneDay);
        List<String> titles = files.stream().map(FileMetadata::getTitle).collect(Collectors.toList());
        return CompletableFuture.completedFuture(titles);
    }
}
