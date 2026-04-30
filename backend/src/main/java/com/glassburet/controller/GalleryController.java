package com.glassburet.controller;

import com.glassburet.dto.PhotoDto;
import com.glassburet.model.Photo;
import com.glassburet.realtime.SiteUpdateBroadcaster;
import com.glassburet.service.GalleryService;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    @Autowired
    private GalleryService galleryService;

    @Autowired
    private SiteUpdateBroadcaster siteUpdateBroadcaster;

    @GetMapping
    public ResponseEntity<List<Photo>> getAllPhotos(@RequestParam(required = false) String category) {
        return ResponseEntity.ok(galleryService.findAll(category));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Photo> uploadPhoto(@RequestBody PhotoDto photoDto) {
        Photo photo = galleryService.create(photoDto);
        siteUpdateBroadcaster.publish("gallery");
        return ResponseEntity.ok(photo);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Photo> uploadFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam(required = false) String caption,
                                           @RequestParam(required = false) String album,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) String uploadedBy) {
        try {
            if (file == null || file.isEmpty()) throw new IllegalArgumentException("File required");
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String filename = UUID.randomUUID().toString() + ext;
            Path target = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), target);

            PhotoDto dto = new PhotoDto();
            dto.setImageUrl("/uploads/" + filename);
            dto.setCaption(caption);
            dto.setAlbum(album);
            dto.setCategory(category);
            dto.setUploadedBy(uploadedBy);
            Photo created = galleryService.create(dto);
            siteUpdateBroadcaster.publish("gallery");
            return ResponseEntity.status(201).body(created);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Photo> updatePhoto(@PathVariable Long id, @RequestBody PhotoDto photoDto) {
        Photo photo = galleryService.update(id, photoDto);
        siteUpdateBroadcaster.publish("gallery");
        return ResponseEntity.ok(photo);
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<Photo> likePhoto(@PathVariable Long id, Authentication auth) {
        Photo photo = galleryService.toggleLike(id, auth.getName());
        siteUpdateBroadcaster.publish("gallery");
        return ResponseEntity.ok(photo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        galleryService.delete(id);
        siteUpdateBroadcaster.publish("gallery");
        return ResponseEntity.noContent().build();
    }
}
