package com.glassburet.repository;

import com.glassburet.model.Event;
import com.glassburet.model.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate from);
    List<Event> findByCategoryAndEventDateGreaterThanEqualOrderByEventDateAsc(EventCategory category, LocalDate from);
    long countByEventDateBetween(LocalDate start, LocalDate end);
}
