package com.glassburet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glassburet.dto.QuoteDto;
import com.glassburet.repository.QuoteRepository;
import com.glassburet.service.QuoteService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class QuotesDataLoader implements ApplicationRunner {

    private final QuoteRepository quoteRepository;
    private final ObjectMapper objectMapper;
    private final QuoteService quoteService;

    public QuotesDataLoader(QuoteRepository quoteRepository, ObjectMapper objectMapper, QuoteService quoteService) {
        this.quoteRepository = quoteRepository;
        this.objectMapper = objectMapper;
        this.quoteService = quoteService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (quoteRepository.count() > 0) return;

        ClassPathResource resource = new ClassPathResource("data/quotes.json");
        if (!resource.exists()) return;

        try (InputStream is = resource.getInputStream()) {
            QuoteDto[] dtos = objectMapper.readValue(is, QuoteDto[].class);
            if (dtos != null) {
                for (QuoteDto dto : dtos) {
                    // create via service to apply same logic
                    try { quoteService.create(dto); } catch (Exception ignored) {}
                }
            }
        }
    }
}
