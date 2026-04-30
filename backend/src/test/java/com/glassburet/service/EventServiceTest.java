package com.glassburet.service;

import com.glassburet.dto.EventDto;
import com.glassburet.model.Event;
import com.glassburet.model.EventCategory;
import com.glassburet.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository repository;

    @InjectMocks
    private EventService service;

    @Test
    void findUpcomingDelegatesWithCurrentDateAndCategory() {
        Event event = new Event();
        when(repository.findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate.now())).thenReturn(List.of(event));
        when(repository.findByCategoryAndEventDateGreaterThanEqualOrderByEventDateAsc(EventCategory.FAGLIG, LocalDate.now()))
                .thenReturn(List.of(event));

        assertThat(service.findUpcoming()).containsExactly(event);
        assertThat(service.findUpcomingByCategory(EventCategory.FAGLIG)).containsExactly(event);
    }

    @Test
    void createAndUpdateCopyDtoFields() {
        Event existing = new Event();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.create(dto("New")).getTitle()).isEqualTo("New");
        assertThat(service.update(1L, dto("Updated")).getTitle()).isEqualTo("Updated");
    }

    @Test
    void toggleAttendanceAddsAndRemovesMemberAndThrowsWhenMissing() {
        Event event = new Event();
        when(repository.findById(1L)).thenReturn(Optional.of(event));
        when(repository.findById(2L)).thenReturn(Optional.empty());
        when(repository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.toggleAttendance(1L, "Ada").getAttendees()).containsExactly("Ada");
        assertThat(service.toggleAttendance(1L, "Ada").getAttendees()).isEmpty();
        assertThatThrownBy(() -> service.toggleAttendance(2L, "Ada"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void deleteDelegates() {
        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    private EventDto dto(String title) {
        EventDto dto = new EventDto();
        dto.setTitle(title);
        dto.setDescription("Description");
        dto.setEventDate(LocalDate.of(2026, 4, 30));
        dto.setTimeStart(LocalTime.of(12, 0));
        dto.setTimeEnd(LocalTime.of(13, 0));
        dto.setLocation("Oslo");
        dto.setCategory(EventCategory.FAGLIG);
        return dto;
    }
}
