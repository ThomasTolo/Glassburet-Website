package com.glassburet.service;

import com.glassburet.dto.WordlePuzzleDto;
import com.glassburet.model.WordlePuzzle;
import com.glassburet.repository.WordlePuzzleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordlePuzzleServiceTest {

    @Mock
    private WordlePuzzleRepository repository;

    @InjectMocks
    private WordlePuzzleService service;

    @Test
    void createNormalizesWordAndDefaultsCreator() {
        WordlePuzzleDto dto = new WordlePuzzleDto();
        dto.setWord(" spill ");
        when(repository.save(any(WordlePuzzle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WordlePuzzle puzzle = service.create(dto);

        assertThat(puzzle.getWord()).isEqualTo("SPILL");
        assertThat(puzzle.getCreatedBy()).isEqualTo("Anonym");
    }

    @Test
    void getDailyReturnsExistingOrCreatesNew() {
        WordlePuzzle existing = new WordlePuzzle();
        existing.setWord("ABOUT");
        when(repository.findByPuzzleDateAndIsDailyTrue(LocalDate.now()))
                .thenReturn(Optional.of(existing), Optional.empty());
        when(repository.save(any(WordlePuzzle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.getDailyPuzzle()).isSameAs(existing);
        WordlePuzzle created = service.getDailyPuzzle();

        assertThat(created.getCreatedBy()).isEqualTo("Glassburet");
        assertThat(created.getIsDaily()).isTrue();
        assertThat(created.getPuzzleDate()).isEqualTo(LocalDate.now());
        assertThat(created.getWord()).isNotBlank();
    }

    @Test
    void latestAndAllDelegate() {
        WordlePuzzle puzzle = new WordlePuzzle();
        when(repository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(puzzle));
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(puzzle));

        assertThat(service.getLatest()).contains(puzzle);
        assertThat(service.getAll()).containsExactly(puzzle);
        verify(repository).findAllByOrderByCreatedAtDesc();
    }
}
