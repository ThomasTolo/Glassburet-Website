package com.glassburet.controller;

import com.glassburet.dto.ConnectionsPuzzleDto;
import com.glassburet.dto.ConnectionsPuzzleResponse;
import com.glassburet.realtime.SiteUpdateBroadcaster;
import com.glassburet.service.ConnectionsPuzzleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<ConnectionsPuzzleResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(service.getAll().stream()
                .map(puzzle -> service.hideAnswers(puzzle, authentication.getName(), canManageAll(authentication)))
                .toList());
    }

    @GetMapping("/daily")
    public ResponseEntity<ConnectionsPuzzleResponse> getDaily(Authentication authentication) {
        return ResponseEntity.ok(service.hideAnswers(service.getDailyPuzzle(), authentication.getName(), canManageAll(authentication)));
    }

    @GetMapping("/latest")
    public ResponseEntity<ConnectionsPuzzleResponse> getLatest(Authentication authentication) {
        return service.getLatest()
                .map(puzzle -> service.hideAnswers(puzzle, authentication.getName(), canManageAll(authentication)))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<ConnectionsPuzzleResponse> validate(@PathVariable Long id, @RequestBody Map<String, List<String>> body, Authentication authentication) {
        return ResponseEntity.ok(service.validateSelection(id, body.get("words"), authentication.getName(), canManageAll(authentication)));
    }

    @PostMapping
    public ResponseEntity<ConnectionsPuzzleResponse> create(@RequestBody ConnectionsPuzzleDto dto, Authentication authentication) {
        dto.setCreatedBy(authentication.getName());
        ConnectionsPuzzleResponse puzzle = service.create(dto);
        siteUpdateBroadcaster.publish("puzzles");
        return ResponseEntity.ok(puzzle);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConnectionsPuzzleResponse> update(@PathVariable Long id, @RequestBody ConnectionsPuzzleDto dto, Authentication authentication) {
        ConnectionsPuzzleResponse puzzle = service.update(id, dto, authentication.getName(), canManageAll(authentication));
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
