package com.glassburet.service;

import com.glassburet.dto.ConnectionsPuzzleDto;
import com.glassburet.dto.ConnectionsPuzzleResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glassburet.model.ConnectionsPuzzle;
import com.glassburet.repository.ConnectionsPuzzleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionsPuzzleServiceTest {

    @Mock
    private ConnectionsPuzzleRepository repository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ConnectionsPuzzleService service;

    @Test
    void createPersistsGroupsAndDefaultsCreator() {
        when(repository.save(any(ConnectionsPuzzle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConnectionsPuzzleResponse response = service.create(dto(null));

        assertThat(response.getCreatedBy()).isEqualTo("Anonym");
        assertThat(response.getGroups()).hasSize(4);
        assertThat(response.getGroups().get(0).getCategory()).isEqualTo("A");
        assertThat(response.getGroups().get(0).getWords()).containsExactly("A1", "A2", "A3", "A4");
    }

    @Test
    void getDailyPuzzleReturnsExistingPuzzle() {
        ConnectionsPuzzle existing = puzzle("Existing", "ONE,TWO,THREE,FOUR");
        existing.setIsDaily(true);
        existing.setPuzzleDate(LocalDate.now());
        when(repository.findByPuzzleDateAndIsDailyTrue(LocalDate.now())).thenReturn(Optional.of(existing));

        ConnectionsPuzzleResponse response = service.getDailyPuzzle();

        assertThat(response.getCreatedBy()).isEqualTo("Existing");
        assertThat(response.isIsDaily()).isTrue();
        assertThat(response.getGroups().get(0).getWords()).containsExactly("ONE", "TWO", "THREE", "FOUR");
    }

    @Test
    void getDailyPuzzleCreatesWhenMissing() {
        when(repository.findByPuzzleDateAndIsDailyTrue(LocalDate.now())).thenReturn(Optional.empty());
        when(repository.save(any(ConnectionsPuzzle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConnectionsPuzzleResponse response = service.getDailyPuzzle();

        assertThat(response.getCreatedBy()).isEqualTo("Glassburet");
        assertThat(response.isIsDaily()).isTrue();
        assertThat(response.getPuzzleDate()).isEqualTo(LocalDate.now());
        assertThat(response.getGroups()).hasSize(4);
    }

    @Test
    void getLatestAndAllMapResponses() {
        ConnectionsPuzzle puzzle = puzzle("Ada", "A,B,C,D");
        when(repository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(puzzle));
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(puzzle));

        assertThat(service.getLatest()).isPresent();
        assertThat(service.getAll()).hasSize(1);
        verify(repository).findAllByOrderByCreatedAtDesc();
    }

    private ConnectionsPuzzleDto dto(String createdBy) {
        ConnectionsPuzzleDto dto = new ConnectionsPuzzleDto();
        dto.setCreatedBy(createdBy);
        dto.setGroups(List.of(group("A"), group("B"), group("C"), group("D")));
        return dto;
    }

    private ConnectionsPuzzleDto.GroupDto group(String category) {
        ConnectionsPuzzleDto.GroupDto group = new ConnectionsPuzzleDto.GroupDto();
        group.setCategory(category);
        group.setWords(List.of(category + "1", category + "2", category + "3", category + "4"));
        return group;
    }

    private ConnectionsPuzzle puzzle(String createdBy, String words) {
        ConnectionsPuzzle puzzle = new ConnectionsPuzzle();
        puzzle.setCreatedBy(createdBy);
        puzzle.setGroup0Category("A");
        puzzle.setGroup0Words(words);
        puzzle.setGroup1Category("B");
        puzzle.setGroup1Words("B1,B2,B3,B4");
        puzzle.setGroup2Category("C");
        puzzle.setGroup2Words("C1,C2,C3,C4");
        puzzle.setGroup3Category("D");
        puzzle.setGroup3Words("D1,D2,D3,D4");
        return puzzle;
    }
}
