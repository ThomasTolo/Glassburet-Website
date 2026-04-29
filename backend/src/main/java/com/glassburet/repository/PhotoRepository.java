package com.glassburet.repository;

import com.glassburet.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByCategoryOrderByUploadedAtDesc(String category);
    List<Photo> findAllByOrderByUploadedAtDesc();
}
