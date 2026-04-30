package com.glassburet.service;

import com.glassburet.dto.LeaderboardEntryDto;
import com.glassburet.dto.ScoreSubmitDto;
import com.glassburet.dto.ScoreSubmissionResult;
import com.glassburet.model.GameName;
import com.glassburet.model.Score;
import com.glassburet.repository.ScoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreServiceTest {

    @Mock
    private ScoreRepository scoreRepository;

    @InjectMocks
    private ScoreService service;

    @Test
    void submitSavesNewPuzzleScore() {
        ScoreSubmitDto dto = dto(42L);
        when(scoreRepository.findByMemberNameAndGameNameAndPuzzleId("Ada", GameName.WORDLE, 42L))
                .thenReturn(Optional.empty());
        when(scoreRepository.save(any(Score.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScoreSubmissionResult result = service.submit(dto);

        assertThat(result.isCreated()).isTrue();
        assertThat(result.getScore().getMemberName()).isEqualTo("Ada");
        assertThat(result.getScore().getPuzzleId()).isEqualTo(42L);
    }

    @Test
    void submitReturnsExistingPuzzleScore() {
        Score existing = new Score();
        when(scoreRepository.findByMemberNameAndGameNameAndPuzzleId("Ada", GameName.WORDLE, 42L))
                .thenReturn(Optional.of(existing));

        ScoreSubmissionResult result = service.submit(dto(42L));

        assertThat(result.isCreated()).isFalse();
        assertThat(result.getScore()).isSameAs(existing);
    }

    @Test
    void submitReturnsExistingDateScoreAfterIntegrityRace() {
        ScoreSubmitDto dto = dto(null);
        Score existing = new Score();
        when(scoreRepository.findByMemberNameAndGameNameAndGameDate("Ada", GameName.WORDLE, dto.getGameDate()))
                .thenReturn(Optional.empty(), Optional.of(existing));
        when(scoreRepository.save(any(Score.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        ScoreSubmissionResult result = service.submit(dto);

        assertThat(result.isCreated()).isFalse();
        assertThat(result.getScore()).isSameAs(existing);
    }

    @Test
    void submitRethrowsIntegrityErrorWhenDuplicateCannotBeFound() {
        ScoreSubmitDto dto = dto(null);
        when(scoreRepository.findByMemberNameAndGameNameAndGameDate("Ada", GameName.WORDLE, dto.getGameDate()))
                .thenReturn(Optional.empty());
        when(scoreRepository.save(any(Score.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.submit(dto))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void completedPuzzleIdsFilterNullsAndInvalidGames() {
        Score first = new Score();
        first.setPuzzleId(1L);
        Score second = new Score();
        when(scoreRepository.findByMemberNameAndGameName("Ada", GameName.WORDLE))
                .thenReturn(List.of(first, second));

        assertThat(service.getCompletedPuzzleIds("Ada", "WORDLE")).containsExactly(1L);
        assertThat(service.getCompletedPuzzleIds("Ada", "NOPE")).isEmpty();
    }

    @Test
    void leaderboardMapsRowsAndSelectsRepositoryBranch() {
        when(scoreRepository.findAllTimeLeaderboardByGame(GameName.WORDLE))
                .thenReturn(Collections.singletonList(new Object[] {"Ada", 12}));

        List<LeaderboardEntryDto> result = service.getLeaderboard("alltime", "WORDLE");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMemberName()).isEqualTo("Ada");
        assertThat(result.get(0).getScore()).isEqualTo(12);
    }

    @Test
    void leaderboardReturnsEmptyForInvalidGameAndDelegatesDateLookup() {
        assertThat(service.getLeaderboard("daily", "NOPE")).isEmpty();

        LocalDate date = LocalDate.of(2026, 4, 30);
        service.getScoresForDate(date);
        verify(scoreRepository).findByGameDate(date);
    }

    private ScoreSubmitDto dto(Long puzzleId) {
        ScoreSubmitDto dto = new ScoreSubmitDto();
        dto.setMemberName("Ada");
        dto.setGameName(GameName.WORDLE);
        dto.setScoreValue(10);
        dto.setGameDate(LocalDate.of(2026, 4, 30));
        dto.setPuzzleId(puzzleId);
        return dto;
    }
}
