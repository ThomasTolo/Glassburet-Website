package com.glassburet.service;

import com.glassburet.dto.PhotoDto;
import com.glassburet.model.Photo;
import com.glassburet.repository.PhotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GalleryService {

    private final PhotoRepository photoRepository;

    public GalleryService(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public List<Photo> findAll(String category) {
        if (category == null || category.isBlank() || category.equalsIgnoreCase("Alt")) {
            return photoRepository.findAllByOrderByUploadedAtDesc();
        }
        return photoRepository.findByCategoryOrderByUploadedAtDesc(category);
    }

    @Transactional
    public Photo create(PhotoDto dto) {
        Photo photo = new Photo();
        photo.setImageUrl(dto.getImageUrl());
        photo.setCaption(dto.getCaption());
        if (dto.getAlbum() != null) photo.setAlbum(dto.getAlbum());
        if (dto.getCategory() != null) photo.setCategory(dto.getCategory());
        photo.setUploadedBy(dto.getUploadedBy());
        return photoRepository.save(photo);
    }

    @Transactional
    public void delete(Long id) {
        photoRepository.deleteById(id);
    }
}
