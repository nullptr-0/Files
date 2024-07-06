package com.nullptr.files.controller;

import com.nullptr.files.entity.FileMetadata;
import com.nullptr.files.service.FileStorageService;
import com.nullptr.files.validation.InputValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FileOperationControllerTest {

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private FileOperationController fileOperationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(fileOperationController).build();
        doNothing().when(fileStorageService).init();
    }

    @Test
    public void testUploadFileValidInput() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        when(fileStorageService.save(any(), anyString(), anyString())).thenReturn(CompletableFuture.completedFuture("File uploaded successfully"));

        MvcResult mvcResult = mockMvc.perform(multipart("/f/ul")
                .file(file)
                .param("title", "Valid Title")
                .param("description", "Valid Description"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded successfully"));
    }

    @Test
    public void testUploadFileInvalidInput() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        MvcResult mvcResult = mockMvc.perform(multipart("/f/ul")
                .file(file)
                .param("title", "Invalid Title'; DROP TABLE file_metadata; --")
                .param("description", "Invalid Description"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid input detected"));
    }

    @Test
    public void testDownloadFileValidTitle() throws Exception {
        when(fileStorageService.loadByTitle(anyString(), any())).thenReturn(CompletableFuture.completedFuture(ResponseEntity.ok().build()));

        MvcResult mvcResult = mockMvc.perform(get("/f/dl/{title}", "Valid Title"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());
    }

    @Test
    public void testDownloadFileInvalidTitle() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/f/dl/{title}", "Invalid Title'; DROP TABLE file_metadata; --"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid input detected"));
    }

    @Test
    public void testFindFilesValidInput() throws Exception {
        when(fileStorageService.findFiles(anyString(), any())).thenReturn(CompletableFuture.completedFuture(List.of("Valid Title")));

        String requestJson = "{\"title\":\"Valid Title\",\"date\":\"2024-06-30T00:00:00\"}";

        MvcResult mvcResult = mockMvc.perform(post("/f/fd")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Valid Title"));
    }

    @Test
    public void testFindFilesInvalidInput() throws Exception {
        String requestJson = "{\"title\":\"Invalid Title'; DROP TABLE file_metadata; --\",\"date\":\"2024-06-30T00:00:00\"}";

        MvcResult mvcResult = mockMvc.perform(post("/f/fd")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    public void testGetFileDetailsValidTitle() throws Exception {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setTitle("Valid Title");
        fileMetadata.setDescription("Valid Description");
        LocalDateTime uploadTime = LocalDateTime.now();
        fileMetadata.setUploadTime(uploadTime);

        when(fileStorageService.getFileDetailsByTitle(anyString())).thenReturn(CompletableFuture.completedFuture(Optional.of(fileMetadata)));

        MvcResult mvcResult = mockMvc.perform(get("/f/dt")
                .param("title", "Valid Title"))
                .andExpect(request().asyncStarted())
                .andReturn();

        String formattedUploadTime = uploadTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").doesNotExist())
                .andExpect(jsonPath("$.title").value("Valid Title"))
                .andExpect(jsonPath("$.description").value("Valid Description"))
                .andExpect(jsonPath("$.uploadTime").value(formattedUploadTime));
    }

    @Test
    public void testGetFileDetailsInvalidTitle() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/f/dt")
                .param("title", "Invalid Title'; DROP TABLE file_metadata; --"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    public void testGetFileDetailsNotFound() throws Exception {
        when(fileStorageService.getFileDetailsByTitle(anyString())).thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        MvcResult mvcResult = mockMvc.perform(get("/f/dt")
                .param("title", "Non-Existent Title"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }
}
