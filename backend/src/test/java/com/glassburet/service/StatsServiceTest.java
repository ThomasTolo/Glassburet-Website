package com.glassburet.service;

import com.glassburet.dto.StatsDto;
import com.glassburet.repository.EventRepository;
import com.glassburet.repository.MemberRepository;
import com.glassburet.repository.QuoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private StatsService service;

    @Test
    void getStatsCombinesRepositoryCounts() {
        when(memberRepository.count()).thenReturn(3L);
        when(quoteRepository.count()).thenReturn(4L);
        when(eventRepository.countByEventDateBetween(any(), any())).thenReturn(5L);

        StatsDto stats = service.getStats();

        assertThat(stats.getMemberCount()).isEqualTo(3L);
        assertThat(stats.getQuoteCount()).isEqualTo(4L);
        assertThat(stats.getActivitiesThisWeek()).isEqualTo(5L);
    }
}
