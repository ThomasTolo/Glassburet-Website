package com.glassburet.controller;

import com.glassburet.dto.PhotoDto;
import com.glassburet.model.Photo;
import com.glassburet.service.GalleryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    @Autowired
    private GalleryService galleryService;

    @GetMapping
    public ResponseEntity<List<Photo>> getAllPhotos(@RequestParam(required = false) String category) {
        return ResponseEntity.ok(galleryService.findAll(category));
    }

    @PostMapping
    public ResponseEntity<Photo> uploadPhoto(@RequestBody PhotoDto photoDto) {
        return ResponseEntity.ok(galleryService.create(photoDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        galleryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
