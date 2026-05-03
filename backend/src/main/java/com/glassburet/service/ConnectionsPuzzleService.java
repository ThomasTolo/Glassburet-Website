package com.glassburet.service;

import com.glassburet.dto.ConnectionsPuzzleDto;
import com.glassburet.dto.ConnectionsPuzzleResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glassburet.model.ConnectionsPuzzle;
import com.glassburet.repository.ConnectionsPuzzleRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ConnectionsPuzzleService {

    private static final String DAILY_RESOURCE_PATH = "games/connections/daily.json";

    private final ConnectionsPuzzleRepository repository;
    private final ObjectMapper objectMapper;
    private final List<ConnectionsPuzzleDto> dailyPuzzles;

    public ConnectionsPuzzleService(ConnectionsPuzzleRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.dailyPuzzles = loadDailyPuzzles();
    }

    private List<ConnectionsPuzzleDto> loadDailyPuzzles() {
        ClassPathResource resource = new ClassPathResource(DAILY_RESOURCE_PATH);
        if (!resource.exists()) {
            return List.of();
        }
        try (InputStream input = resource.getInputStream()) {
            return objectMapper.readValue(
                    input,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ConnectionsPuzzleDto.class)
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load daily Connections puzzles: " + DAILY_RESOURCE_PATH, e);
        }
    }

    @Transactional
    public ConnectionsPuzzleResponse create(ConnectionsPuzzleDto dto) {
        ConnectionsPuzzle puzzle = new ConnectionsPuzzle();
        puzzle.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "Anonym");
        applyDto(puzzle, dto);

        return toResponse(repository.save(puzzle));
    }

    @Transactional
    public ConnectionsPuzzleResponse update(Long id, ConnectionsPuzzleDto dto, String ownerName, boolean canOverride) {
        ConnectionsPuzzle puzzle = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        requireEditable(puzzle, ownerName, canOverride);
        applyDto(puzzle, dto);
        return toResponse(repository.save(puzzle));
    }

    @Transactional
    public void delete(Long id, String ownerName, boolean canOverride) {
        ConnectionsPuzzle puzzle = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        requireEditable(puzzle, ownerName, canOverride);
        repository.delete(puzzle);
    }

    @Transactional
    public ConnectionsPuzzleResponse getDailyPuzzle() {
        LocalDate today = LocalDate.now();
        Optional<ConnectionsPuzzle> existing = repository.findByPuzzleDateAndIsDailyTrue(today);
        if (existing.isPresent()) return toResponse(existing.get());

        ConnectionsPuzzle puzzle = new ConnectionsPuzzle();
        puzzle.setCreatedBy("Glassburet");
        puzzle.setTitle("Daglig Tankeburet");
        puzzle.setIsDaily(true);
        puzzle.setPuzzleDate(today);
        applyGroups(puzzle, dailyPuzzleFor(today).getGroups());

        return toResponse(repository.save(puzzle));
    }

    private ConnectionsPuzzleDto dailyPuzzleFor(LocalDate date) {
        if (dailyPuzzles.isEmpty()) {
            throw new IllegalArgumentException("No daily Connections puzzles configured");
        }
        long dayIndex = date.toEpochDay() - LocalDate.of(2026, 1, 1).toEpochDay();
        int idx = (int) (((dayIndex % dailyPuzzles.size()) + dailyPuzzles.size()) % dailyPuzzles.size());
        return dailyPuzzles.get(idx);
    }

    public Optional<ConnectionsPuzzleResponse> getLatest() {
        return repository.findTopByOrderByCreatedAtDesc().map(this::toResponse);
    }

    public List<ConnectionsPuzzleResponse> getAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    public ConnectionsPuzzleResponse hideAnswers(ConnectionsPuzzleResponse response, String viewerName, boolean canOverride) {
        if (response == null || canOverride || (viewerName != null && viewerName.equals(response.getCreatedBy()))) {
            return response;
        }
        List<String> words = response.getGroups().stream()
                .flatMap(group -> group.getWords().stream())
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        Collections.shuffle(words);
        response.setWords(words);
        response.setGroups(null);
        return response;
    }

    public ConnectionsPuzzleResponse validateSelection(Long id, List<String> selected, String viewerName, boolean canOverride) {
        ConnectionsPuzzleResponse response = repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (selected == null || selected.size() != 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return response.getGroups().stream()
                .filter(group -> selected.stream().allMatch(group.getWords()::contains))
                .findFirst()
                .map(group -> {
                    ConnectionsPuzzleResponse result = new ConnectionsPuzzleResponse();
                    result.setId(response.getId());
                    result.setGroups(List.of(group));
                    return result;
                })
                .orElseGet(() -> {
                    ConnectionsPuzzleResponse result = new ConnectionsPuzzleResponse();
                    result.setId(response.getId());
                    result.setGroups(List.of());
                    return result;
                });
    }

    private ConnectionsPuzzleResponse toResponse(ConnectionsPuzzle p) {
        ConnectionsPuzzleResponse resp = new ConnectionsPuzzleResponse();
        resp.setId(p.getId());
        resp.setCreatedBy(p.getCreatedBy());
        resp.setTitle(p.getTitle());
        resp.setCreatedAt(p.getCreatedAt());
        resp.setIsDaily(Boolean.TRUE.equals(p.getIsDaily()));
        resp.setPuzzleDate(p.getPuzzleDate());
        resp.setGroups(List.of(
            new ConnectionsPuzzleResponse.Group(p.getGroup0Category(), split(p.getGroup0Words()), 0),
            new ConnectionsPuzzleResponse.Group(p.getGroup1Category(), split(p.getGroup1Words()), 1),
            new ConnectionsPuzzleResponse.Group(p.getGroup2Category(), split(p.getGroup2Words()), 2),
            new ConnectionsPuzzleResponse.Group(p.getGroup3Category(), split(p.getGroup3Words()), 3)
        ));
        return resp;
    }

    private List<String> split(String words) {
        return Arrays.asList(words.split(","));
    }

    private void applyDto(ConnectionsPuzzle puzzle, ConnectionsPuzzleDto dto) {
        puzzle.setTitle(clean(dto.getTitle()));
        applyGroups(puzzle, dto.getGroups());
    }

    private void applyGroups(ConnectionsPuzzle puzzle, List<ConnectionsPuzzleDto.GroupDto> groups) {
        puzzle.setGroup0Category(groups.get(0).getCategory());
        puzzle.setGroup0Words(String.join(",", groups.get(0).getWords()));
        puzzle.setGroup1Category(groups.get(1).getCategory());
        puzzle.setGroup1Words(String.join(",", groups.get(1).getWords()));
        puzzle.setGroup2Category(groups.get(2).getCategory());
        puzzle.setGroup2Words(String.join(",", groups.get(2).getWords()));
        puzzle.setGroup3Category(groups.get(3).getCategory());
        puzzle.setGroup3Words(String.join(",", groups.get(3).getWords()));
    }

    private void requireEditable(ConnectionsPuzzle puzzle, String ownerName, boolean canOverride) {
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
}
