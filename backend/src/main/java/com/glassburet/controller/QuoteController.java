package com.glassburet.controller;

import com.glassburet.dto.QuoteDto;
import com.glassburet.model.Quote;
import com.glassburet.service.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuote(@PathVariable Long id) {
        quoteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
