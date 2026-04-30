package com.glassburet.service;

import com.glassburet.dto.QuoteDto;
import com.glassburet.model.Quote;
import com.glassburet.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.time.LocalDateTime;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;

    public QuoteService(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    public List<Quote> findAll() {
        return quoteRepository.findAll();
    }

    public Quote findById(Long id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quote not found: " + id));
    }

    public Quote findFeatured() {
        // Prefer a quote created today (latest). If none, return the most recent quote.
        try {
            var today = java.time.LocalDate.now();
            var start = today.atStartOfDay();
            var end = start.plusDays(1);
            return quoteRepository.findTopByCreatedAtBetweenOrderByCreatedAtDesc(start, end)
                    .or(() -> quoteRepository.findTopByOrderByCreatedAtDesc())
                    .orElseThrow(() -> new NoSuchElementException("No quotes found"));
        } catch (NoSuchElementException e) {
            throw e;
        }
    }

    @Transactional
    public Quote create(QuoteDto dto) {
        Quote quote = new Quote();
        quote.setText(dto.getText());
        quote.setAuthor(dto.getAuthor());
        quote.setFeatured(dto.isFeatured());
        if (dto.getCreatedAt() != null && !dto.getCreatedAt().isBlank()) {
            try {
                quote.setCreatedAt(LocalDateTime.parse(dto.getCreatedAt()));
            } catch (Exception ignored) {
            }
        }
        return quoteRepository.save(quote);
    }

    @Transactional
    public Quote update(Long id, QuoteDto dto) {
        Quote quote = findById(id);
        quote.setText(dto.getText());
        quote.setAuthor(dto.getAuthor());
        quote.setFeatured(dto.isFeatured());
        if (dto.getCreatedAt() != null && !dto.getCreatedAt().isBlank()) {
            try {
                quote.setCreatedAt(LocalDateTime.parse(dto.getCreatedAt()));
            } catch (Exception ignored) {
            }
        }
        return quoteRepository.save(quote);
    }

    @Transactional
    public Quote toggleLike(Long id, String memberName) {
        Quote quote = findById(id);
        if (quote.getLikes().contains(memberName)) {
            quote.getLikes().remove(memberName);
        } else {
            quote.getLikes().add(memberName);
        }
        return quoteRepository.save(quote);
    }

    @Transactional
    public void delete(Long id) {
        quoteRepository.deleteById(id);
    }
}
