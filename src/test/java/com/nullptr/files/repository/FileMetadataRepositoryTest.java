package com.nullptr.files.repository;

import com.nullptr.files.entity.FileMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class FileMetadataRepositoryTest {

    @Autowired
    private FileMetadataRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        FileMetadata file1 = new FileMetadata();
        file1.setFilename("test_file_1.txt");
        file1.setTitle("Test Title 1");
        file1.setDescription("This is a test description for test_file_1.txt.");
        file1.setUploadTime(LocalDateTime.now().minusDays(1));
        repository.save(file1);

        FileMetadata file2 = new FileMetadata();
        file2.setFilename("test_file_2.txt");
        file2.setTitle("Test Title 2");
        file2.setDescription("This is a test description for test_file_2.txt.");
        file2.setUploadTime(LocalDateTime.now());
        repository.save(file2);
    }

    @Test
    public void testFindByTitle() {
        Optional<FileMetadata> fileMetadata = repository.findByTitle("Test Title 1");
        assertTrue(fileMetadata.isPresent());
        assertEquals("test_file_1.txt", fileMetadata.get().getFilename());
    }

    @Test
    public void testFindByTitleAndDate() {
        // get a time before the upload time (1 hour ago here)
        LocalDateTime queriedTime = LocalDateTime.now().minusHours(1);
        LocalDateTime queriedTimePlusOneDay = queriedTime.plusDays(1);
        List<FileMetadata> files = repository.findByTitleAndDate("Test Title 2", queriedTime, queriedTimePlusOneDay);
        assertEquals(1, files.size());
        assertEquals("test_file_2.txt", files.get(0).getFilename());
    }
}
