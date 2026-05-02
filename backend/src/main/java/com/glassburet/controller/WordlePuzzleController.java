package com.glassburet.controller;

import com.glassburet.dto.WordlePuzzleDto;
import com.glassburet.model.WordlePuzzle;
import com.glassburet.realtime.SiteUpdateBroadcaster;
import com.glassburet.service.WordlePuzzleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/puzzles/wordle")
public class WordlePuzzleController {

    private final WordlePuzzleService service;
    private final SiteUpdateBroadcaster siteUpdateBroadcaster;

    public WordlePuzzleController(WordlePuzzleService service, SiteUpdateBroadcaster siteUpdateBroadcaster) {
        this.service = service;
        this.siteUpdateBroadcaster = siteUpdateBroadcaster;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(Authentication authentication) {
        return ResponseEntity.ok(service.getAll().stream()
                .map(puzzle -> service.toResponse(puzzle, authentication.getName(), canManageAll(authentication)))
                .toList());
    }

    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDaily(Authentication authentication) {
        return ResponseEntity.ok(service.toResponse(service.getDailyPuzzle(), authentication.getName(), canManageAll(authentication)));
    }

    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatest(Authentication authentication) {
        return service.getLatest()
                .map(puzzle -> service.toResponse(puzzle, authentication.getName(), canManageAll(authentication)))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<Map<String, Object>> validate(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.validateGuess(id, body.get("guess")));
    }

    @PostMapping
    public ResponseEntity<WordlePuzzle> create(@RequestBody WordlePuzzleDto dto, Authentication authentication) {
        dto.setCreatedBy(authentication.getName());
        WordlePuzzle puzzle = service.create(dto);
        siteUpdateBroadcaster.publish("puzzles");
        return ResponseEntity.ok(puzzle);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WordlePuzzle> update(@PathVariable Long id, @RequestBody WordlePuzzleDto dto, Authentication authentication) {
        WordlePuzzle puzzle = service.update(id, dto, authentication.getName(), canManageAll(authentication));
        siteUpdateBroadcaster.publish("puzzles");
        return ResponseEntity.ok(puzzle);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication.getName(), canManageAll(authentication));
        siteUpdateBroadcaster.publish("puzzles");
        return ResponseEntity.noContent().build();
    }

    private boolean canManageAll(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_OWNER"));
    }
}
