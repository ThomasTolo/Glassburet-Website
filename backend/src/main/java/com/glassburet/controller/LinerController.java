package com.glassburet.controller;

import com.glassburet.dto.LinerDto;
import com.glassburet.model.Liner;
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

    @GetMapping
    public ResponseEntity<List<Liner>> getAllLiners() {
        return ResponseEntity.ok(linerService.findAll());
    }

    @PostMapping
    public ResponseEntity<Liner> createLiner(@RequestBody LinerDto linerDto) {
        return ResponseEntity.ok(linerService.create(linerDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Liner> updateLiner(@PathVariable Long id, @RequestBody LinerDto linerDto) {
        return ResponseEntity.ok(linerService.update(id, linerDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLiner(@PathVariable Long id) {
        linerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/said")
    public ResponseEntity<Liner> incrementSaidCount(@PathVariable Long id) {
        return ResponseEntity.ok(linerService.incrementSaidCount(id));
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<Liner> likeLiner(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(linerService.toggleLike(id, auth.getName()));
    }
}
