package com.nullptr.files.repository;

import com.nullptr.files.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByTitle(String title);

    @Query("SELECT f FROM FileMetadata f WHERE (:title IS NULL OR f.title = :title) AND (:date IS NULL OR f.uploadTime >= :date AND f.uploadTime < :datePlusOneDay)")
    List<FileMetadata> findByTitleAndDate(@Param("title") String title, @Param("date") LocalDateTime date, @Param("datePlusOneDay") LocalDateTime datePlusOneDay);
}
