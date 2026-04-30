package com.glassburet.controller;

import com.glassburet.dto.WordlePuzzleDto;
import com.glassburet.model.WordlePuzzle;
import com.glassburet.service.WordlePuzzleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/puzzles/wordle")
public class WordlePuzzleController {

    private final WordlePuzzleService service;

    public WordlePuzzleController(WordlePuzzleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<WordlePuzzle>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/daily")
    public ResponseEntity<WordlePuzzle> getDaily() {
        return ResponseEntity.ok(service.getDailyPuzzle());
    }

    @GetMapping("/latest")
    public ResponseEntity<WordlePuzzle> getLatest() {
        return service.getLatest()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<WordlePuzzle> create(@RequestBody WordlePuzzleDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }
}
