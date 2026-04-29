package com.glassburet.service;

import com.glassburet.dto.WordlePuzzleDto;
import com.glassburet.model.WordlePuzzle;
import com.glassburet.repository.WordlePuzzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WordlePuzzleService {

    private final WordlePuzzleRepository repository;

    public WordlePuzzleService(WordlePuzzleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WordlePuzzle create(WordlePuzzleDto dto) {
        WordlePuzzle puzzle = new WordlePuzzle();
        puzzle.setWord(dto.getWord().toUpperCase().trim());
        puzzle.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "Anonym");
        return repository.save(puzzle);
    }

    public Optional<WordlePuzzle> getLatest() {
        return repository.findTopByOrderByCreatedAtDesc();
    }
}
