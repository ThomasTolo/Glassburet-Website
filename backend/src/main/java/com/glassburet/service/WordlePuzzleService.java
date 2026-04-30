package com.glassburet.service;

import com.glassburet.dto.WordlePuzzleDto;
import com.glassburet.model.WordlePuzzle;
import com.glassburet.repository.WordlePuzzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class WordlePuzzleService {

    private final WordlePuzzleRepository repository;

    private static final String[] DAILY_WORDS = {
        "SPILL","NORSK","FJORD","BILDE","GLASS","KRAFT","SMART","STORM",
        "NOTER","PLASS","SKOLE","KLART","SNILL","FLOTT","STERK","VOKSE",
        "VARMT","KALDT","VENTE","SISTE","LYSER","BRUKE","DAGER","ELSKE",
        "FINNE","GRØNN","HENTE","JOBBE","KJØRE","LAGET","MASSE","NESTE",
        "RASKT","TROLL","UNDER","VERDI","YNGRE","FARGE","TOLKE","ÅPENT",
    };

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

    @Transactional
    public WordlePuzzle getDailyPuzzle() {
        LocalDate today = LocalDate.now();
        Optional<WordlePuzzle> existing = repository.findByPuzzleDateAndIsDailyTrue(today);
        if (existing.isPresent()) return existing.get();

        long dayIndex = today.toEpochDay() - LocalDate.of(2026, 1, 1).toEpochDay();
        int idx = (int) (((dayIndex % DAILY_WORDS.length) + DAILY_WORDS.length) % DAILY_WORDS.length);

        WordlePuzzle puzzle = new WordlePuzzle();
        puzzle.setWord(DAILY_WORDS[idx]);
        puzzle.setCreatedBy("Glassburet");
        puzzle.setIsDaily(true);
        puzzle.setPuzzleDate(today);
        return repository.save(puzzle);
    }

    public Optional<WordlePuzzle> getLatest() {
        return repository.findTopByOrderByCreatedAtDesc();
    }

    public List<WordlePuzzle> getAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }
}
