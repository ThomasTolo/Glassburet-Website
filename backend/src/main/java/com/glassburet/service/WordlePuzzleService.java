package com.glassburet.service;

import com.glassburet.dto.WordlePuzzleDto;
import com.glassburet.model.WordlePuzzle;
import com.glassburet.repository.WordlePuzzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Locale;

@Service
public class WordlePuzzleService {

    private final WordlePuzzleRepository repository;
    private final List<String> answerWords;
    private final Set<String> allowedGuessWords;

    public WordlePuzzleService(WordlePuzzleRepository repository) {
        this.repository = repository;
        this.answerWords = List.copyOf(loadWordList("wordle/answerWords.txt"));
        LinkedHashSet<String> allowed = new LinkedHashSet<>(answerWords);
        allowed.addAll(loadWordList("wordle/guessWords.txt"));
        this.allowedGuessWords = Set.copyOf(allowed);
    }

    @Transactional
    public WordlePuzzle create(WordlePuzzleDto dto) {
        WordlePuzzle puzzle = new WordlePuzzle();
        puzzle.setWord(cleanAnswerWord(dto.getWord()));
        puzzle.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "Anonym");
        puzzle.setTitle(clean(dto.getTitle()));
        return repository.save(puzzle);
    }

    @Transactional
    public WordlePuzzle update(Long id, WordlePuzzleDto dto, String ownerName, boolean canOverride) {
        WordlePuzzle puzzle = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        requireEditable(puzzle, ownerName, canOverride);
        puzzle.setWord(cleanAnswerWord(dto.getWord()));
        puzzle.setTitle(clean(dto.getTitle()));
        return repository.save(puzzle);
    }

    @Transactional
    public void delete(Long id, String ownerName, boolean canOverride) {
        WordlePuzzle puzzle = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        requireEditable(puzzle, ownerName, canOverride);
        repository.delete(puzzle);
    }

    @Transactional
    public WordlePuzzle getDailyPuzzle() {
        LocalDate today = LocalDate.now();
        Optional<WordlePuzzle> existing = repository.findByPuzzleDateAndIsDailyTrue(today);
        String dailyAnswer = dailyAnswerFor(today);
        if (existing.isPresent()) {
            WordlePuzzle puzzle = existing.get();
            if (!answerWords.contains(puzzle.getWord())) {
                puzzle.setWord(dailyAnswer);
                return repository.save(puzzle);
            }
            return puzzle;
        }

        WordlePuzzle puzzle = new WordlePuzzle();
        puzzle.setWord(dailyAnswer);
        puzzle.setCreatedBy("Glassburet");
        puzzle.setTitle("Daglig Ordburet");
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

    public Map<String, Object> toResponse(WordlePuzzle puzzle, String viewerName, boolean canOverride) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", puzzle.getId());
        response.put("createdBy", puzzle.getCreatedBy());
        response.put("title", puzzle.getTitle());
        response.put("createdAt", puzzle.getCreatedAt());
        response.put("isDaily", Boolean.TRUE.equals(puzzle.getIsDaily()));
        response.put("puzzleDate", puzzle.getPuzzleDate());
        if (canOverride || (viewerName != null && viewerName.equals(puzzle.getCreatedBy()))) {
            response.put("word", puzzle.getWord());
        }
        return response;
    }

    public Map<String, Object> validateGuess(Long id, String guess) {
        WordlePuzzle puzzle = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String cleanGuess = guess == null ? "" : guess.trim().toUpperCase();
        String target = puzzle.getWord();
        if (cleanGuess.length() != target.length()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (!allowedGuessWords.contains(cleanGuess)) {
            return Map.of("validWord", false, "message", "Ikke i ordlisten");
        }
        String[] states = new String[cleanGuess.length()];
        java.util.Arrays.fill(states, "absent");
        Map<Character, Integer> remaining = new java.util.HashMap<>();
        for (int i = 0; i < target.length(); i++) {
            if (cleanGuess.charAt(i) == target.charAt(i)) {
                states[i] = "correct";
            } else {
                remaining.merge(target.charAt(i), 1, Integer::sum);
            }
        }
        for (int i = 0; i < cleanGuess.length(); i++) {
            if ("correct".equals(states[i])) continue;
            char letter = cleanGuess.charAt(i);
            int count = remaining.getOrDefault(letter, 0);
            if (count > 0) {
                states[i] = "present";
                remaining.put(letter, count - 1);
            }
        }
        return Map.of("validWord", true, "states", List.of(states), "correct", cleanGuess.equals(target));
    }

    private void requireEditable(WordlePuzzle puzzle, String ownerName, boolean canOverride) {
        if (Boolean.TRUE.equals(puzzle.getIsDaily())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!canOverride && (ownerName == null || !ownerName.equals(puzzle.getCreatedBy()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String cleanAnswerWord(String value) {
        String word = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!answerWords.contains(word)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return word;
    }

    private String dailyAnswerFor(LocalDate date) {
        long dayIndex = date.toEpochDay() - LocalDate.of(2026, 1, 1).toEpochDay();
        int idx = (int) (((dayIndex % answerWords.size()) + answerWords.size()) % answerWords.size());
        return answerWords.get(idx);
    }

    private List<String> loadWordList(String path) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(path),
                StandardCharsets.UTF_8
        ))) {
            List<String> words = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toUpperCase(Locale.ROOT);
                if (word.length() == 5) {
                    words.add(word);
                }
            }
            if (words.isEmpty()) {
                throw new IllegalStateException("Wordle word list is empty: " + path);
            }
            return words;
        } catch (Exception e) {
            throw new IllegalStateException("Could not load Wordle word list: " + path, e);
        }
    }
}
