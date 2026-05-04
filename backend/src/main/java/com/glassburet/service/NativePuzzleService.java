package com.glassburet.service;

import com.glassburet.dto.NativePuzzleDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.glassburet.model.GameName;
import com.glassburet.model.NativePuzzle;
import com.glassburet.repository.NativePuzzleRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NativePuzzleService {

    private final NativePuzzleRepository repository;
    private final ObjectMapper objectMapper;

    private static final Map<GameName, String> DAILY_RESOURCE_PATHS = Map.of(
            GameName.MORE_OR_LESS, "games/native/more-or-less.json",
            GameName.SONGLESS, "games/native/songless.json",
            GameName.CROSSTUNES, "games/native/crosstunes.json",
            GameName.TIMEGUESSR, "games/native/timeguessr.json",
            GameName.ARTISTBURET, "games/native/artistburet.json"
    );

    private final Map<GameName, List<String>> dailyPayloads;

    public NativePuzzleService(NativePuzzleRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.dailyPayloads = loadDailyPayloads();
    }

    private Map<GameName, List<String>> loadDailyPayloads() {
        Map<GameName, List<String>> payloads = new EnumMap<>(GameName.class);
        for (Map.Entry<GameName, String> entry : DAILY_RESOURCE_PATHS.entrySet()) {
            ClassPathResource resource = new ClassPathResource(entry.getValue());
            if (!resource.exists()) {
                continue;
            }
            try (InputStream input = resource.getInputStream()) {
                JsonNode root = objectMapper.readTree(input);
                List<String> gamePayloads = new ArrayList<>();
                if (root.isArray()) {
                    for (JsonNode payload : root) {
                        gamePayloads.add(objectMapper.writeValueAsString(payload));
                    }
                } else if (root.has("daily")) {
                    // Compact format: { "pool": [...], "daily": [...] }
                    // Inject pool into each daily entry so validate can access it.
                    JsonNode pool = root.path("pool");
                    for (JsonNode daily : root.withArray("daily")) {
                        if (daily.isObject()) {
                            ObjectNode combined = (ObjectNode) daily.deepCopy();
                            if (!pool.isMissingNode()) {
                                combined.set("artists", pool);
                            }
                            gamePayloads.add(objectMapper.writeValueAsString(combined));
                        }
                    }
                }
                if (!gamePayloads.isEmpty()) {
                    payloads.put(entry.getKey(), gamePayloads);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load daily native puzzles: " + entry.getValue(), e);
            }
        }
        return payloads;
    }

    public List<NativePuzzle> getAll(GameName gameName) {
        return repository.findByGameNameOrderByCreatedAtDesc(gameName);
    }

    public NativePuzzle hideAnswers(NativePuzzle puzzle, String viewerName, boolean canOverride) {
        if (puzzle == null || canOverride || (viewerName != null && viewerName.equals(puzzle.getCreatedBy()))) {
            return puzzle;
        }
        NativePuzzle safe = copyPuzzle(puzzle);
        safe.setPayloadJson(safePayload(puzzle.getGameName(), puzzle.getPayloadJson()));
        return safe;
    }

    @Transactional
    public NativePuzzle getDaily(GameName gameName) {
        LocalDate today = LocalDate.now();
        return repository.findByGameNameAndPuzzleDateAndIsDailyTrue(gameName, today)
                .map(puzzle -> refreshCrosstunesDailyIfNeeded(gameName, puzzle, today))
                .map(puzzle -> refreshSonglessDailyIfNeeded(gameName, puzzle, today))
                .map(puzzle -> refreshTimeguessrDailyIfNeeded(gameName, puzzle, today))
                .map(puzzle -> refreshArtistburetDailyIfNeeded(gameName, puzzle, today))
                .orElseGet(() -> createDaily(gameName, today));
    }

    @Transactional
    public NativePuzzle create(GameName gameName, NativePuzzleDto dto) {
        NativePuzzle puzzle = new NativePuzzle();
        puzzle.setGameName(gameName);
        puzzle.setCreatedBy(dto.getCreatedBy() != null && !dto.getCreatedBy().isBlank() ? dto.getCreatedBy() : "Anonym");
        puzzle.setTitle(clean(dto.getTitle()));
        puzzle.setPayloadJson(dto.getPayloadJson());
        return repository.save(puzzle);
    }

    @Transactional
    public NativePuzzle update(GameName gameName, Long id, NativePuzzleDto dto, String ownerName, boolean canOverride) {
        NativePuzzle puzzle = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (puzzle.getGameName() != gameName) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        requireEditable(puzzle, ownerName, canOverride);
        puzzle.setTitle(clean(dto.getTitle()));
        puzzle.setPayloadJson(dto.getPayloadJson());
        return repository.save(puzzle);
    }

    @Transactional
    public void delete(GameName gameName, Long id, String ownerName, boolean canOverride) {
        NativePuzzle puzzle = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (puzzle.getGameName() != gameName) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        requireEditable(puzzle, ownerName, canOverride);
        repository.delete(puzzle);
    }

    private NativePuzzle createDaily(GameName gameName, LocalDate date) {
        NativePuzzle puzzle = new NativePuzzle();
        puzzle.setGameName(gameName);
        puzzle.setCreatedBy("Glassburet");
        puzzle.setTitle(dailyTitle(gameName));
        puzzle.setIsDaily(true);
        puzzle.setPuzzleDate(date);
        puzzle.setPayloadJson(dailyPayloadFor(gameName, date));
        return repository.save(puzzle);
    }

    private NativePuzzle refreshCrosstunesDailyIfNeeded(GameName gameName, NativePuzzle puzzle, LocalDate date) {
        if (gameName != GameName.CROSSTUNES || puzzle.getPayloadJson() == null || puzzle.getPayloadJson().contains("\"dailySetVersion\":9")) {
            return puzzle;
        }
        puzzle.setPayloadJson(dailyPayloadFor(gameName, date));
        return repository.save(puzzle);
    }

    private NativePuzzle refreshSonglessDailyIfNeeded(GameName gameName, NativePuzzle puzzle, LocalDate date) {
        if (gameName != GameName.SONGLESS || puzzle.getPayloadJson() == null || puzzle.getPayloadJson().contains("\"songlessVersion\":3")) {
            return puzzle;
        }
        puzzle.setPayloadJson(dailyPayloadFor(gameName, date));
        return repository.save(puzzle);
    }

    private NativePuzzle refreshTimeguessrDailyIfNeeded(GameName gameName, NativePuzzle puzzle, LocalDate date) {
        if (gameName != GameName.TIMEGUESSR || puzzle.getPayloadJson() == null || puzzle.getPayloadJson().contains("\"timeguessrVersion\":4")) {
            return puzzle;
        }
        puzzle.setPayloadJson(dailyPayloadFor(gameName, date));
        return repository.save(puzzle);
    }

    private NativePuzzle refreshArtistburetDailyIfNeeded(GameName gameName, NativePuzzle puzzle, LocalDate date) {
        if (gameName != GameName.ARTISTBURET || puzzle.getPayloadJson() == null || puzzle.getPayloadJson().contains("\"artistburetVersion\":2")) {
            return puzzle;
        }
        puzzle.setPayloadJson(dailyPayloadFor(gameName, date));
        return repository.save(puzzle);
    }

    private String dailyPayloadFor(GameName gameName, LocalDate date) {
        List<String> payloads = dailyPayloads.get(gameName);
        if (payloads == null || payloads.isEmpty()) {
            throw new IllegalArgumentException("Unsupported native game");
        }
        long dayIndex = date.toEpochDay() - LocalDate.of(2026, 1, 1).toEpochDay();
        int idx = (int) (((dayIndex % payloads.size()) + payloads.size()) % payloads.size());
        return payloads.get(idx);
    }

    private void requireEditable(NativePuzzle puzzle, String ownerName, boolean canOverride) {
        if (Boolean.TRUE.equals(puzzle.getIsDaily())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!canOverride && (ownerName == null || !ownerName.equals(puzzle.getCreatedBy()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private String dailyTitle(GameName gameName) {
        return switch (gameName) {
            case MORE_OR_LESS -> "Daglig Merburet";
            case SONGLESS -> "Daglig Låtburet";
            case CROSSTUNES -> "Daglig Kryssburet";
            case TIMEGUESSR -> "Daglig Tidsburet";
            case ARTISTBURET -> "Daglig Artistburet";
            default -> "Daglig Spill";
        };
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Map<String, Object> validate(GameName gameName, Long id, Map<String, Object> body) {
        NativePuzzle puzzle = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (puzzle.getGameName() != gameName) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        try {
            JsonNode payload = objectMapper.readTree(puzzle.getPayloadJson());
            return switch (gameName) {
                case MORE_OR_LESS -> validateMoreOrLess(payload, body);
                case SONGLESS -> validateSongless(payload, body);
                case CROSSTUNES -> validateCrosstunes(payload, body);
                case TIMEGUESSR -> validateTimeguessr(payload, body);
                case ARTISTBURET -> validateArtistburet(payload, body);
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            };
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private NativePuzzle copyPuzzle(NativePuzzle source) {
        NativePuzzle copy = new NativePuzzle();
        copy.setGameName(source.getGameName());
        copy.setCreatedBy(source.getCreatedBy());
        copy.setTitle(source.getTitle());
        copy.setIsDaily(source.getIsDaily());
        copy.setPuzzleDate(source.getPuzzleDate());
        copy.setPayloadJson(source.getPayloadJson());
        try {
            java.lang.reflect.Field id = NativePuzzle.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(copy, source.getId());
            java.lang.reflect.Field createdAt = NativePuzzle.class.getDeclaredField("createdAt");
            createdAt.setAccessible(true);
            createdAt.set(copy, source.getCreatedAt());
        } catch (ReflectiveOperationException ignored) {}
        return copy;
    }

    private String safePayload(GameName gameName, String payloadJson) {
        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(payloadJson);
            if (gameName == GameName.MORE_OR_LESS && root.has("rounds")) {
                for (JsonNode round : root.withArray("rounds")) {
                    ((ObjectNode) round).remove("answer");
                }
            }
            if (gameName == GameName.SONGLESS) {
                root.remove("answer");
                root.remove("title");
            }
            if (gameName == GameName.CROSSTUNES && root.has("clues")) {
                for (JsonNode clue : root.withArray("clues")) {
                    ObjectNode clueObject = (ObjectNode) clue;
                    String answer = clueObject.path("answer").asText("");
                    clueObject.put("length", answer.length());
                    clueObject.remove("answer");
                }
            }
            if (gameName == GameName.TIMEGUESSR) {
                hideTimeguessrAnswer(root);
            }
            if (gameName == GameName.ARTISTBURET) {
                root.remove("answer");
                root.remove("debut");
                root.remove("members");
                root.remove("gender");
                root.remove("genre");
                root.remove("country");
                root.remove("popularity");
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, Object> validateMoreOrLess(JsonNode payload, Map<String, Object> body) {
        int index = ((Number) body.getOrDefault("index", 0)).intValue();
        String choice = String.valueOf(body.getOrDefault("choice", ""));
        ArrayNode rounds = (ArrayNode) payload.path("rounds");
        if (index < 0 || index >= rounds.size()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        boolean correct = choice.equals(rounds.get(index).path("answer").asText());
        return Map.of("correct", correct);
    }

    private Map<String, Object> validateSongless(JsonNode payload, Map<String, Object> body) {
        String guess = normalize(String.valueOf(body.getOrDefault("guess", "")));
        String answer = payload.path("answer").asText(payload.path("title").asText(""));
        boolean correct = guess.equals(normalize(answer));
        return Map.of("correct", correct, "answer", answer);
    }

    private Map<String, Object> validateCrosstunes(JsonNode payload, Map<String, Object> body) {
        Map<?, ?> input = body.get("input") instanceof Map<?, ?> map ? map : Map.of();
        boolean complete = true;
        boolean correct = true;
        for (JsonNode clue : payload.withArray("clues")) {
            String answer = clue.path("answer").asText("");
            int row = clue.path("row").asInt();
            int col = clue.path("col").asInt();
            String dir = clue.path("dir").asText();
            for (int i = 0; i < answer.length(); i++) {
                int r = "D".equals(dir) ? row + i : row;
                int c = "A".equals(dir) ? col + i : col;
                Object value = input.get(r + "," + c);
                if (value == null || String.valueOf(value).isBlank()) complete = false;
                else if (!String.valueOf(value).equalsIgnoreCase(String.valueOf(answer.charAt(i)))) correct = false;
            }
        }
        return Map.of("complete", complete, "correct", complete && correct);
    }

    private Map<String, Object> validateTimeguessr(JsonNode payload, Map<String, Object> body) {
        int guessYear = ((Number) body.getOrDefault("year", 0)).intValue();
        double guessLat = ((Number) body.getOrDefault("lat", 0)).doubleValue();
        double guessLng = ((Number) body.getOrDefault("lng", 0)).doubleValue();
        JsonNode round = timeguessrRound(payload, ((Number) body.getOrDefault("roundIndex", 0)).intValue());
        int year = round.path("year").asInt();
        double lat = round.path("lat").asDouble();
        double lng = round.path("lng").asDouble();
        double distanceKm = distanceKm(guessLat, guessLng, lat, lng);
        int locationScore = (int) Math.max(0, Math.round(500 - (distanceKm / 10)));
        int yearScore = Math.max(0, 500 - Math.abs(guessYear - year) * 20);
        return Map.of(
                "score", locationScore + yearScore,
                "locationScore", locationScore,
                "yearScore", yearScore,
                "distanceKm", Math.round(distanceKm),
                "year", year,
                "lat", lat,
                "lng", lng,
                "locationName", round.path("locationName").asText(round.path("place").asText(""))
        );
    }

    private JsonNode timeguessrRound(JsonNode payload, int roundIndex) {
        if (!payload.has("rounds")) {
            return payload;
        }
        ArrayNode rounds = (ArrayNode) payload.path("rounds");
        if (roundIndex < 0 || roundIndex >= rounds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return rounds.get(roundIndex);
    }

    private void hideTimeguessrAnswer(ObjectNode node) {
        if (node.has("rounds")) {
            for (JsonNode round : node.withArray("rounds")) {
                hideTimeguessrAnswer((ObjectNode) round);
            }
            return;
        }
        node.remove("year");
        node.remove("place");
        node.remove("lat");
        node.remove("lng");
        node.remove("locationName");
    }

    private Map<String, Object> validateArtistburet(JsonNode payload, Map<String, Object> body) {
        String guess = String.valueOf(body.getOrDefault("guess", "")).trim().toLowerCase();
        ArrayNode artists = (ArrayNode) payload.path("artists");
        JsonNode guessedArtist = null;
        for (JsonNode artist : artists) {
            if (artist.path("name").asText("").toLowerCase().equals(guess)) {
                guessedArtist = artist;
                break;
            }
        }
        if (guessedArtist == null) {
            return Map.of("correct", false, "notFound", true);
        }
        String answer = payload.path("answer").asText("");
        boolean correct = answer.toLowerCase().equals(guess);
        int guessDebut = guessedArtist.path("debut").asInt();
        int answerDebut = payload.path("debut").asInt();
        int guessPop = guessedArtist.path("popularity").asInt();
        int answerPop = payload.path("popularity").asInt();
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("correct", correct);
        result.put("notFound", false);
        result.put("name", guessedArtist.path("name").asText());
        result.put("debut", guessDebut);
        result.put("members", guessedArtist.path("members").asText());
        result.put("gender", guessedArtist.path("gender").asText());
        result.put("genre", guessedArtist.path("genre").asText());
        result.put("country", guessedArtist.path("country").asText());
        result.put("popularity", guessPop);
        result.put("imageUrl", guessedArtist.path("imageUrl").asText(""));
        if (correct) {
            result.put("debutStatus", "match");
            result.put("debutDirection", "none");
            result.put("popularityStatus", "match");
            result.put("popularityDirection", "none");
            result.put("membersStatus", "match");
            result.put("membersDirection", "none");
            result.put("genderStatus", "match");
            result.put("genreStatus", "match");
            result.put("countryStatus", "match");
        } else {
            int debutDiff = Math.abs(guessDebut - answerDebut);
            result.put("debutStatus", guessDebut == answerDebut ? "match" : (debutDiff <= 5 ? "close" : "no-match"));
            result.put("debutDirection", guessDebut < answerDebut ? "up" : (guessDebut > answerDebut ? "down" : "none"));
            int popDiff = Math.abs(guessPop - answerPop);
            result.put("popularityStatus", guessPop == answerPop ? "match" : (popDiff <= 50 ? "close" : "no-match"));
            result.put("popularityDirection", guessPop < answerPop ? "down" : (guessPop > answerPop ? "up" : "none"));
            result.put("membersStatus", artistMembersStatus(guessedArtist.path("members").asText(), payload.path("members").asText()));
            result.put("membersDirection", artistMembersDirection(guessedArtist.path("members").asText(), payload.path("members").asText()));
            result.put("genderStatus", guessedArtist.path("gender").asText().equals(payload.path("gender").asText()) ? "match" : "no-match");
            result.put("genreStatus", guessedArtist.path("genre").asText().equals(payload.path("genre").asText()) ? "match" : "no-match");
            result.put("countryStatus", guessedArtist.path("country").asText().equals(payload.path("country").asText()) ? "match" : "no-match");
        }
        return result;
    }

    private String artistMembersStatus(String guessedMembers, String answerMembers) {
        if (guessedMembers.equals(answerMembers)) {
            return "match";
        }
        return isArtistGroup(guessedMembers) && isArtistGroup(answerMembers) ? "close" : "no-match";
    }

    private String artistMembersDirection(String guessedMembers, String answerMembers) {
        int guessedCount = artistMemberCount(guessedMembers);
        int answerCount = artistMemberCount(answerMembers);
        return guessedCount < answerCount ? "up" : (guessedCount > answerCount ? "down" : "none");
    }

    private int artistMemberCount(String members) {
        if (members == null || members.equalsIgnoreCase("Solo")) {
            return 1;
        }
        Matcher matcher = Pattern.compile("\\d+").matcher(members);
        return matcher.find() ? Integer.parseInt(matcher.group()) : 1;
    }

    private boolean isArtistGroup(String members) {
        return members != null && !members.equalsIgnoreCase("Solo") && members.toLowerCase().contains("member");
    }

    private double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private String normalize(String value) {
        return value == null ? "" : value.toUpperCase().replaceAll("[^A-Z0-9]", "");
    }
}
