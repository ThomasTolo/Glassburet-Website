package com.glassburet.controller;

import com.glassburet.dto.NativePuzzleDto;
import com.glassburet.model.GameName;
import com.glassburet.model.NativePuzzle;
import com.glassburet.realtime.SiteUpdateBroadcaster;
import com.glassburet.service.NativePuzzleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/puzzles/native/{gameName}")
public class NativePuzzleController {

    private final NativePuzzleService service;
    private final SiteUpdateBroadcaster siteUpdateBroadcaster;

    public NativePuzzleController(NativePuzzleService service, SiteUpdateBroadcaster siteUpdateBroadcaster) {
        this.service = service;
        this.siteUpdateBroadcaster = siteUpdateBroadcaster;
    }

    @GetMapping
    public ResponseEntity<List<NativePuzzle>> getAll(@PathVariable GameName gameName) {
        return ResponseEntity.ok(service.getAll(gameName));
    }

    @GetMapping("/daily")
    public ResponseEntity<NativePuzzle> getDaily(@PathVariable GameName gameName) {
        return ResponseEntity.ok(service.getDaily(gameName));
    }

    @PostMapping
    public ResponseEntity<NativePuzzle> create(@PathVariable GameName gameName, @RequestBody NativePuzzleDto dto, Authentication authentication) {
        dto.setCreatedBy(authentication.getName());
        NativePuzzle puzzle = service.create(gameName, dto);
        siteUpdateBroadcaster.publish("puzzles");
        return ResponseEntity.ok(puzzle);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NativePuzzle> update(@PathVariable GameName gameName, @PathVariable Long id, @RequestBody NativePuzzleDto dto, Authentication authentication) {
        NativePuzzle puzzle = service.update(gameName, id, dto, authentication.getName(), canManageAll(authentication));
        siteUpdateBroadcaster.publish("puzzles");
        return ResponseEntity.ok(puzzle);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable GameName gameName, @PathVariable Long id, Authentication authentication) {
        service.delete(gameName, id, authentication.getName(), canManageAll(authentication));
        siteUpdateBroadcaster.publish("puzzles");
        return ResponseEntity.noContent().build();
    }

    private boolean canManageAll(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_OWNER"));
    }
}
