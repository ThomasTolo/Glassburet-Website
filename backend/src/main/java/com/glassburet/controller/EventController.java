package com.glassburet.controller;

import com.glassburet.dto.EventDto;
import com.glassburet.model.Event;
import com.glassburet.model.EventCategory;
import com.glassburet.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping
    public ResponseEntity<List<Event>> getUpcomingEvents() {
        return ResponseEntity.ok(eventService.findUpcoming());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Event>> getUpcomingByCategory(@PathVariable EventCategory category) {
        return ResponseEntity.ok(eventService.findUpcomingByCategory(category));
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody EventDto eventDto) {
        return ResponseEntity.ok(eventService.create(eventDto));
    }

    @PutMapping("/{eventId}/attendance/{memberName}")
    public ResponseEntity<Event> toggleAttendance(@PathVariable Long eventId, @PathVariable String memberName) {
        return ResponseEntity.ok(eventService.toggleAttendance(eventId, memberName));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
