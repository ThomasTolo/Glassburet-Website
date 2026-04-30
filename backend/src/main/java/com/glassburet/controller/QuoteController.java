package com.glassburet.controller;

import com.glassburet.dto.QuoteDto;
import com.glassburet.model.Quote;
import com.glassburet.realtime.SiteUpdateBroadcaster;
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

    @Autowired
    private SiteUpdateBroadcaster siteUpdateBroadcaster;

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
        Quote quote = quoteService.create(quoteDto);
        siteUpdateBroadcaster.publish("quotes");
        return ResponseEntity.ok(quote);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quote> updateQuote(@PathVariable Long id, @RequestBody QuoteDto quoteDto) {
        Quote quote = quoteService.update(id, quoteDto);
        siteUpdateBroadcaster.publish("quotes");
        return ResponseEntity.ok(quote);
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<Quote> likeQuote(@PathVariable Long id, Authentication auth) {
        Quote quote = quoteService.toggleLike(id, auth.getName());
        siteUpdateBroadcaster.publish("quotes");
        return ResponseEntity.ok(quote);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuote(@PathVariable Long id) {
        quoteService.delete(id);
        siteUpdateBroadcaster.publish("quotes");
        return ResponseEntity.noContent().build();
    }
}
