package com.glassburet.service;

import com.glassburet.dto.NativePuzzleDto;
import com.glassburet.model.GameName;
import com.glassburet.model.NativePuzzle;
import com.glassburet.repository.NativePuzzleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NativePuzzleServiceTest {

    @Mock
    private NativePuzzleRepository repository;

    @InjectMocks
    private NativePuzzleService service;

    @Test
    void getAllDelegatesByGameName() {
        service.getAll(GameName.SONGLESS);

        verify(repository).findByGameNameOrderByCreatedAtDesc(GameName.SONGLESS);
    }

    @Test
    void createDefaultsBlankCreator() {
        NativePuzzleDto dto = new NativePuzzleDto();
        dto.setCreatedBy(" ");
        dto.setPayloadJson("{}");
        when(repository.save(any(NativePuzzle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NativePuzzle result = service.create(GameName.SONGLESS, dto);

        assertThat(result.getGameName()).isEqualTo(GameName.SONGLESS);
        assertThat(result.getCreatedBy()).isEqualTo("Anonym");
        assertThat(result.getPayloadJson()).isEqualTo("{}");
    }

    @Test
    void getDailyReturnsExistingPuzzle() {
        NativePuzzle existing = new NativePuzzle();
        when(repository.findByGameNameAndPuzzleDateAndIsDailyTrue(GameName.SONGLESS, LocalDate.now()))
                .thenReturn(Optional.of(existing));

        assertThat(service.getDaily(GameName.SONGLESS)).isSameAs(existing);
    }

    @Test
    void getDailyCreatesSupportedGameAndRejectsUnsupportedGame() {
        when(repository.findByGameNameAndPuzzleDateAndIsDailyTrue(GameName.SONGLESS, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(repository.save(any(NativePuzzle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NativePuzzle created = service.getDaily(GameName.SONGLESS);

        assertThat(created.getCreatedBy()).isEqualTo("Glassburet");
        assertThat(created.getIsDaily()).isTrue();
        assertThat(created.getPuzzleDate()).isEqualTo(LocalDate.now());
        assertThat(created.getPayloadJson()).isNotBlank();

        when(repository.findByGameNameAndPuzzleDateAndIsDailyTrue(GameName.GEOGUESSR, LocalDate.now()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getDaily(GameName.GEOGUESSR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported native game");
    }
}
