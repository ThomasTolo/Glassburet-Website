package com.glassburet.service;

import com.glassburet.dto.PhotoDto;
import com.glassburet.model.Photo;
import com.glassburet.repository.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GalleryServiceTest {

    @Mock
    private PhotoRepository repository;

    @InjectMocks
    private GalleryService service;

    @Test
    void findAllUsesAllForBlankOrAltAndCategoryOtherwise() {
        Photo photo = new Photo();
        when(repository.findAllByOrderByUploadedAtDesc()).thenReturn(List.of(photo));
        when(repository.findByCategoryOrderByUploadedAtDesc("Fest")).thenReturn(List.of(photo));

        assertThat(service.findAll(null)).containsExactly(photo);
        assertThat(service.findAll("Alt")).containsExactly(photo);
        assertThat(service.findAll("Fest")).containsExactly(photo);
    }

    @Test
    void createSetsOptionalFieldsOnlyWhenPresent() {
        when(repository.save(any(Photo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Photo created = service.create(dto("caption", "Album", "Fest"));
        Photo defaults = service.create(dto(null, null, null));

        assertThat(created.getAlbum()).isEqualTo("Album");
        assertThat(created.getCategory()).isEqualTo("Fest");
        assertThat(defaults.getAlbum()).isEqualTo("Glassburet");
        assertThat(defaults.getCategory()).isEqualTo("Alt");
    }

    @Test
    void updateChangesNonNullFieldsOnlyAndThrowsWhenMissing() {
        Photo photo = new Photo();
        photo.setCaption("old");
        photo.setCategory("Old");
        when(repository.findById(1L)).thenReturn(Optional.of(photo));
        when(repository.findById(2L)).thenReturn(Optional.empty());
        when(repository.save(any(Photo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Photo updated = service.update(1L, dto("new", null, "New"));

        assertThat(updated.getCaption()).isEqualTo("new");
        assertThat(updated.getAlbum()).isEqualTo("Glassburet");
        assertThat(updated.getCategory()).isEqualTo("New");
        assertThatThrownBy(() -> service.update(2L, dto(null, null, null)))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void toggleLikeAddsAndRemovesMemberAndDeleteDelegates() {
        Photo photo = new Photo();
        when(repository.findById(1L)).thenReturn(Optional.of(photo));
        when(repository.save(any(Photo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.toggleLike(1L, "Ada").getLikes()).containsExactly("Ada");
        assertThat(service.toggleLike(1L, "Ada").getLikes()).isEmpty();
        service.delete(1L);
        verify(repository).deleteById(1L);
    }

    private PhotoDto dto(String caption, String album, String category) {
        PhotoDto dto = new PhotoDto();
        dto.setImageUrl("/image.png");
        dto.setCaption(caption);
        dto.setAlbum(album);
        dto.setCategory(category);
        dto.setUploadedBy("Ada");
        return dto;
    }
}
