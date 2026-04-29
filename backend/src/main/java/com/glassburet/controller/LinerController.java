package com.glassburet.controller;

import com.glassburet.model.Liner;
import com.glassburet.service.LinerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PutMapping("/{id}/said")
    public ResponseEntity<Liner> incrementSaidCount(@PathVariable Long id) {
        return ResponseEntity.ok(linerService.incrementSaidCount(id));
    }
}
