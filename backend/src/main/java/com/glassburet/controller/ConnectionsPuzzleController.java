package com.glassburet.controller;

import com.glassburet.dto.ConnectionsPuzzleDto;
import com.glassburet.dto.ConnectionsPuzzleResponse;
import com.glassburet.realtime.SiteUpdateBroadcaster;
import com.glassburet.service.ConnectionsPuzzleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/puzzles/connections")
public class ConnectionsPuzzleController {

    private final ConnectionsPuzzleService service;
    private final SiteUpdateBroadcaster siteUpdateBroadcaster;

    public ConnectionsPuzzleController(ConnectionsPuzzleService service, SiteUpdateBroadcaster siteUpdateBroadcaster) {
        this.service = service;
        this.siteUpdateBroadcaster = siteUpdateBroadcaster;
    }

    @GetMapping
    public ResponseEntity<List<ConnectionsPuzzleResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/daily")
    public ResponseEntity<ConnectionsPuzzleResponse> getDaily() {
        return ResponseEntity.ok(service.getDailyPuzzle());
    }

    @GetMapping("/latest")
    public ResponseEntity<ConnectionsPuzzleResponse> getLatest() {
        return service.getLatest()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<ConnectionsPuzzleResponse> create(@RequestBody ConnectionsPuzzleDto dto) {
        ConnectionsPuzzleResponse puzzle = service.create(dto);
        siteUpdateBroadcaster.publish("puzzles");
        return ResponseEntity.ok(puzzle);
    }
}
