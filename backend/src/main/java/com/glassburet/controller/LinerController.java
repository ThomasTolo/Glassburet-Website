package com.glassburet.controller;

import com.glassburet.dto.LinerDto;
import com.glassburet.model.Liner;
import com.glassburet.realtime.SiteUpdateBroadcaster;
import com.glassburet.service.LinerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/liners")
public class LinerController {

    @Autowired
    private LinerService linerService;

    @Autowired
    private SiteUpdateBroadcaster siteUpdateBroadcaster;

    @GetMapping
    public ResponseEntity<List<Liner>> getAllLiners() {
        return ResponseEntity.ok(linerService.findAll());
    }

    @PostMapping
    public ResponseEntity<Liner> createLiner(@RequestBody LinerDto linerDto) {
        Liner liner = linerService.create(linerDto);
        siteUpdateBroadcaster.publish("liners");
        return ResponseEntity.ok(liner);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Liner> updateLiner(@PathVariable Long id, @RequestBody LinerDto linerDto) {
        Liner liner = linerService.update(id, linerDto);
        siteUpdateBroadcaster.publish("liners");
        return ResponseEntity.ok(liner);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLiner(@PathVariable Long id) {
        linerService.delete(id);
        siteUpdateBroadcaster.publish("liners");
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/said")
    public ResponseEntity<Liner> incrementSaidCount(@PathVariable Long id) {
        Liner liner = linerService.incrementSaidCount(id);
        siteUpdateBroadcaster.publish("liners");
        return ResponseEntity.ok(liner);
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<Liner> likeLiner(@PathVariable Long id, Authentication auth) {
        Liner liner = linerService.toggleLike(id, auth.getName());
        siteUpdateBroadcaster.publish("liners");
        return ResponseEntity.ok(liner);
    }
}
