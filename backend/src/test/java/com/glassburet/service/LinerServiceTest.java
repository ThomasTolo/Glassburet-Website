package com.glassburet.service;

import com.glassburet.dto.LinerDto;
import com.glassburet.model.Liner;
import com.glassburet.repository.LinerRepository;
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
class LinerServiceTest {

    @Mock
    private LinerRepository repository;

    @InjectMocks
    private LinerService service;

    @Test
    void findAllAndCreateUseRepositoryOrderingAndNextNumber() {
        Liner previous = new Liner();
        previous.setNumber(7);
        when(repository.findAllByOrderByNumberAsc()).thenReturn(List.of(previous));
        when(repository.findTopByOrderByNumberDesc()).thenReturn(Optional.of(previous));
        when(repository.save(any(Liner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.findAll()).containsExactly(previous);
        Liner created = service.create(dto("Text"));

        assertThat(created.getNumber()).isEqualTo(8);
        assertThat(created.getText()).isEqualTo("Text");
    }

    @Test
    void updateChangesExistingLinerAndThrowsWhenMissing() {
        Liner liner = new Liner();
        when(repository.findById(1L)).thenReturn(Optional.of(liner));
        when(repository.findById(2L)).thenReturn(Optional.empty());
        when(repository.save(any(Liner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.update(1L, dto("Updated")).getText()).isEqualTo("Updated");
        assertThatThrownBy(() -> service.update(2L, dto("Missing")))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void incrementAndToggleLikeMutateExistingLiner() {
        Liner liner = new Liner();
        liner.setSaidCount(3);
        when(repository.findById(1L)).thenReturn(Optional.of(liner));
        when(repository.save(any(Liner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.incrementSaidCount(1L).getSaidCount()).isEqualTo(4);
        assertThat(service.toggleLike(1L, "Ada").getLikes()).containsExactly("Ada");
        assertThat(service.toggleLike(1L, "Ada").getLikes()).isEmpty();
    }

    @Test
    void deleteDelegates() {
        service.delete(5L);

        verify(repository).deleteById(5L);
    }

    private LinerDto dto(String text) {
        LinerDto dto = new LinerDto();
        dto.setText(text);
        dto.setAuthor("Ada");
        dto.setSinceYear(2026);
        return dto;
    }
}
