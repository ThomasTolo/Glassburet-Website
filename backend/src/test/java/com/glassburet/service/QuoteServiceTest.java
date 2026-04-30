package com.glassburet.service;

import com.glassburet.dto.QuoteDto;
import com.glassburet.model.Quote;
import com.glassburet.repository.QuoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private QuoteRepository repository;

    @InjectMocks
    private QuoteService service;

    @Test
    void findAllFindByIdAndDeleteDelegate() {
        Quote quote = new Quote();
        when(repository.findAll()).thenReturn(List.of(quote));
        when(repository.findById(1L)).thenReturn(Optional.of(quote));

        assertThat(service.findAll()).containsExactly(quote);
        assertThat(service.findById(1L)).isSameAs(quote);
        service.delete(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(9L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void findFeaturedPrefersTodaysQuoteThenLatestAndThrowsWhenNone() {
        Quote today = new Quote();
        Quote latest = new Quote();
        when(repository.findTopByCreatedAtBetweenOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.of(today), Optional.empty(), Optional.empty());
        when(repository.findTopByOrderByCreatedAtDesc())
                .thenReturn(Optional.of(latest), Optional.empty());

        assertThat(service.findFeatured()).isSameAs(today);
        assertThat(service.findFeatured()).isSameAs(latest);
        assertThatThrownBy(() -> service.findFeatured())
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No quotes found");
    }

    @Test
    void createAndUpdateParseValidCreatedAtAndIgnoreInvalidCreatedAt() {
        Quote existing = new Quote();
        existing.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Quote created = service.create(dto("2026-04-30T14:30"));
        Quote updated = service.update(1L, dto("not-a-date"));

        assertThat(created.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 4, 30, 14, 30));
        assertThat(updated.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    @Test
    void toggleLikeAddsAndRemovesMember() {
        Quote quote = new Quote();
        when(repository.findById(1L)).thenReturn(Optional.of(quote));
        when(repository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.toggleLike(1L, "Ada").getLikes()).containsExactly("Ada");
        assertThat(service.toggleLike(1L, "Ada").getLikes()).isEmpty();
    }

    private QuoteDto dto(String createdAt) {
        QuoteDto dto = new QuoteDto();
        dto.setText("Quote");
        dto.setAuthor("Ada");
        dto.setCreatedAt(createdAt);
        dto.setFeatured(true);
        return dto;
    }
}
