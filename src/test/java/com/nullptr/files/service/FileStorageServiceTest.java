package com.nullptr.files.service;

import com.nullptr.files.entity.FileMetadata;
import com.nullptr.files.repository.FileMetadataRepository;
import com.nullptr.files.config.FileStorageConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.MockitoAnnotations;

import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class FileStorageServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @InjectMocks
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Autowired
    private ApplicationContext context;

    @Test
    public void testSave() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(new FileMetadata());

        CompletableFuture<String> result = fileStorageService.save(file, "Test Title", "Test Description");
        assertEquals("File uploaded successfully", result.join());
    }

    @Test
    public void testLoadByTitle() throws IOException {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFilename("test.txt");
        when(fileMetadataRepository.findByTitle("Test Title")).thenReturn(Optional.of(fileMetadata));

        CompletableFuture<ResponseEntity<?>> result = fileStorageService.loadByTitle("Test Title", null);
        assertEquals(200, result.join().getStatusCodeValue());
    }

    @Test
    public void testFindFiles() {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setTitle("Test Title");
        fileMetadata.setUploadTime(LocalDateTime.now());

        when(fileMetadataRepository.findByTitleAndDate(anyString(), any(), any())).thenReturn(List.of(fileMetadata));

        // get a time before the upload time (1 hour ago here)
        CompletableFuture<List<String>> result = fileStorageService.findFiles("Test Title", LocalDateTime.now().minusHours(1));
        assertEquals(1, result.join().size());
        assertEquals("Test Title", result.join().get(0));
    }
}
