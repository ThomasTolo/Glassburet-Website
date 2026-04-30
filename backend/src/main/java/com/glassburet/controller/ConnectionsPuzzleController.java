package com.glassburet.controller;

import com.glassburet.dto.ConnectionsPuzzleDto;
import com.glassburet.dto.ConnectionsPuzzleResponse;
import com.glassburet.service.ConnectionsPuzzleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/puzzles/connections")
public class ConnectionsPuzzleController {

    private final ConnectionsPuzzleService service;

    public ConnectionsPuzzleController(ConnectionsPuzzleService service) {
        this.service = service;
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
        return ResponseEntity.ok(service.create(dto));
    }
}
