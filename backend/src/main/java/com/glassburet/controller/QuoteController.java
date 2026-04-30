package com.glassburet.controller;

import com.glassburet.dto.QuoteDto;
import com.glassburet.model.Quote;
import com.glassburet.service.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    @Autowired
    private QuoteService quoteService;

    @GetMapping
    public ResponseEntity<List<Quote>> getAllQuotes() {
        return ResponseEntity.ok(quoteService.findAll());
    }

    @GetMapping("/featured")
    public ResponseEntity<Quote> getFeaturedQuote() {
        return ResponseEntity.ok(quoteService.findFeatured());
    }

    @PostMapping
    public ResponseEntity<Quote> createQuote(@RequestBody QuoteDto quoteDto) {
        return ResponseEntity.ok(quoteService.create(quoteDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quote> updateQuote(@PathVariable Long id, @RequestBody QuoteDto quoteDto) {
        return ResponseEntity.ok(quoteService.update(id, quoteDto));
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<Quote> likeQuote(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(quoteService.toggleLike(id, auth.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuote(@PathVariable Long id) {
        quoteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
