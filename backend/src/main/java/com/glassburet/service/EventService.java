package com.glassburet.service;

import com.glassburet.dto.EventDto;
import com.glassburet.model.Event;
import com.glassburet.model.EventCategory;
import com.glassburet.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> findUpcoming() {
        return eventRepository.findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate.now());
    }

    public List<Event> findUpcomingByCategory(EventCategory category) {
        return eventRepository.findByCategoryAndEventDateGreaterThanEqualOrderByEventDateAsc(category, LocalDate.now());
    }

    @Transactional
    public Event create(EventDto dto) {
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setTimeStart(dto.getTimeStart());
        event.setTimeEnd(dto.getTimeEnd());
        event.setLocation(dto.getLocation());
        event.setCategory(dto.getCategory());
        return eventRepository.save(event);
    }

    @Transactional
    public Event toggleAttendance(Long eventId, String memberName) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found: " + eventId));
        if (event.getAttendees().contains(memberName)) {
            event.getAttendees().remove(memberName);
        } else {
            event.getAttendees().add(memberName);
        }
        return eventRepository.save(event);
    }

    @Transactional
    public Event update(Long id, EventDto dto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Event not found: " + id));
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setTimeStart(dto.getTimeStart());
        event.setTimeEnd(dto.getTimeEnd());
        event.setLocation(dto.getLocation());
        event.setCategory(dto.getCategory());
        return eventRepository.save(event);
    }

    @Transactional
    public void delete(Long id) {
        eventRepository.deleteById(id);
    }
}
