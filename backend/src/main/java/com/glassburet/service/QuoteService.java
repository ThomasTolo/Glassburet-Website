package com.glassburet.service;

import com.glassburet.dto.QuoteDto;
import com.glassburet.model.Quote;
import com.glassburet.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

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
        return quoteRepository.findFirstByFeaturedTrue()
                .or(quoteRepository::findRandom)
                .orElseThrow(() -> new NoSuchElementException("No quotes found"));
    }

    @Transactional
    public Quote create(QuoteDto dto) {
        Quote quote = new Quote();
        quote.setText(dto.getText());
        quote.setAuthor(dto.getAuthor());
        quote.setFeatured(dto.isFeatured());
        return quoteRepository.save(quote);
    }

    @Transactional
    public void delete(Long id) {
        quoteRepository.deleteById(id);
    }
}
