package com.glassburet.service;

import com.glassburet.dto.StatsDto;
import com.glassburet.repository.EventRepository;
import com.glassburet.repository.MemberRepository;
import com.glassburet.repository.QuoteRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Service
public class StatsService {

    private final MemberRepository memberRepository;
    private final QuoteRepository quoteRepository;
    private final EventRepository eventRepository;

    public StatsService(MemberRepository memberRepository, QuoteRepository quoteRepository, EventRepository eventRepository) {
        this.memberRepository = memberRepository;
        this.quoteRepository = quoteRepository;
        this.eventRepository = eventRepository;
    }

    public StatsDto getStats() {
        long memberCount = memberRepository.count();
        long quoteCount = quoteRepository.count();

        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        long activitiesThisWeek = eventRepository.countByEventDateBetween(monday, sunday);

        return new StatsDto(memberCount, quoteCount, activitiesThisWeek);
    }
}
