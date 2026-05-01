package com.glassburet.service;

import com.glassburet.dto.NativePuzzleDto;
import com.glassburet.model.GameName;
import com.glassburet.model.NativePuzzle;
import com.glassburet.repository.NativePuzzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class NativePuzzleService {

    private final NativePuzzleRepository repository;

    private static final String[][] CROSSTUNES_SONG_CLUES = new String[][] {
            {"1", "D", "0", "5", "ALISON", "Elvis Costello title name", "1440841151", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/43/fa/6c/43fa6cf2-67cb-d7bf-ce5f-025ace161823/mzaf_16714337406633485841.plus.aac.p.m4a"},
            {"2", "A", "1", "3", "JOLENE", "Dolly Parton title name", "1062400330", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/43/ef/95/43ef956b-4a1c-8ab1-a843-4ac691dc45d9/mzaf_17631698820105622615.plus.aac.p.m4a"},
            {"3", "D", "2", "8", "HELLO", "Adele title", "1544494392", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/93/22/22/93222271-8d55-d923-e0ff-b2964a5abefe/mzaf_3513742103157153222.plus.aac.p.m4a"},
            {"4", "A", "3", "3", "ROSIE", "Whole Lotta ___", "574124731", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/9e/92/cd/9e92cd05-1b13-3984-bb80-8ea8a64db5a1/mzaf_16972473481734511142.plus.aac.p.m4a"},
            {"5", "A", "5", "0", "RHIANNON", "Fleetwood Mac title name", "912323296", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/10/8f/8c/108f8cf8-a414-53a6-94fc-0f6187f4d6b8/mzaf_9354339013232719807.plus.aac.p.m4a"},
            {"6", "A", "7", "3", "MANDY", "Barry Manilow title name", "177410267", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/f2/6f/31/f26f31e0-77be-6baa-7aff-5b8bbde51fef/mzaf_15437302951014621778.plus.aac.p.m4a"},
            {"7", "D", "7", "3", "MARY", "Scissor Sisters title name", "1445880101", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/92/90/05/929005bd-977b-1211-96d7-3f27d66bfa95/mzaf_14114859229761080665.plus.aac.p.m4a"},
            {"8", "A", "8", "3", "ALONE", "Heart title", "725786574", "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/48/6c/2a/486c2a0b-1857-5653-64d1-d8719b1b9930/mzaf_14556555269457036172.plus.aac.p.m4a"}
    };

    private static final String[] CROSSTUNES_DAILY_PAYLOADS = new String[] {
            crosstunesPayload("What's her name? 01", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 02", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 03", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 04", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 05", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 06", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 07", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 08", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 09", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 10", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 11", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 12", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 13", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 14", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 15", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 16", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 17", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 18", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 19", CROSSTUNES_SONG_CLUES),
            crosstunesPayload("What's her name? 20", CROSSTUNES_SONG_CLUES)
    };

    private static final String[] SONGLESS_DAILY_PAYLOADS = new String[] {
            songlessPayload("a-ha", "TAKE ON ME", 380907765, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/f2/03/4f/f2034f41-707f-7111-bc63-e5d3cf7f2240/mzaf_17215043934336702540.plus.aac.p.m4a"),
            songlessPayload("Kygo", "FIRESTONE", 1713785280, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview126/v4/a8/f4/27/a8f42729-3fab-5b8e-9702-8a818d1b8fad/mzaf_15234497499095842632.plus.aac.p.m4a"),
            songlessPayload("AURORA", "RUNAWAY", 1640380713, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview122/v4/34/12/66/34126650-f244-46fc-a7c1-8969e7df765c/mzaf_16801053975927778101.plus.aac.p.m4a"),
            songlessPayload("Ed Sheeran", "SHAPE OF YOU", 1193701392, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/44/c7/4f/44c74f0d-72dc-6143-d4d0-ba14d661ca0d/mzaf_9566898362556366703.plus.aac.p.m4a"),
            songlessPayload("Billie Eilish", "BAD GUY", 1450695739, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/c3/87/1f/c3871f7e-3260-d615-1c66-5fdca2c3a48f/mzaf_10721331211699880949.plus.aac.p.m4a"),
            songlessPayload("The Weeknd", "BLINDING LIGHTS", 1488408568, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/17/b4/8f/17b48f9a-0b93-6bb8-fe1d-3a16623c2cfb/mzaf_9560252727299052414.plus.aac.p.m4a"),
            songlessPayload("Dua Lipa", "LEVITATING", 1538003843, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/59/dc/4d/59dc4dda-93ff-8f1c-c536-f005f6ea6af5/mzaf_3066686759813252385.plus.aac.p.m4a"),
            songlessPayload("Harry Styles", "WATERMELON SUGAR", 1485802967, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/16/86/f5/1686f50d-8b77-7e32-85f7-5f0e804d68fe/mzaf_14195633304344507287.plus.aac.p.m4a")
    };

    private static final Map<GameName, String[]> DAILY_PAYLOADS = Map.of(
            GameName.MORE_OR_LESS, new String[] {
                    "{\"rounds\":[{\"metric\":\"Hvilket land har flest innbyggere?\",\"left\":\"Norge\",\"right\":\"Danmark\",\"answer\":\"right\"},{\"metric\":\"Hvilket fjell er høyest?\",\"left\":\"Galdhøpiggen\",\"right\":\"Gaustatoppen\",\"answer\":\"left\"},{\"metric\":\"Hvilken by ligger lengst nord?\",\"left\":\"Trondheim\",\"right\":\"Bergen\",\"answer\":\"left\"},{\"metric\":\"Hvilken hendelse er nyest?\",\"left\":\"HTTP/2 standardiseres\",\"right\":\"Vue lanseres\",\"answer\":\"left\"},{\"metric\":\"Hva tar vanligvis lengst tid?\",\"left\":\"Koke pasta\",\"right\":\"Brygge espresso\",\"answer\":\"left\"}]}",
                    "{\"rounds\":[{\"metric\":\"Hva har flest tangenter?\",\"left\":\"Piano\",\"right\":\"Datatastatur\",\"answer\":\"left\"},{\"metric\":\"Hvilket hav er størst?\",\"left\":\"Atlanterhavet\",\"right\":\"Stillehavet\",\"answer\":\"right\"},{\"metric\":\"Hvilket språk har flest bokstaver her?\",\"left\":\"JavaScript\",\"right\":\"Go\",\"answer\":\"left\"},{\"metric\":\"Hva veier mest?\",\"left\":\"En liter vann\",\"right\":\"Et vanlig eple\",\"answer\":\"left\"},{\"metric\":\"Hvilket tall er høyest?\",\"left\":\"2^10\",\"right\":\"10^2\",\"answer\":\"left\"}]}"
            },
            GameName.SONGLESS, SONGLESS_DAILY_PAYLOADS,
            GameName.CROSSTUNES, CROSSTUNES_DAILY_PAYLOADS,
            GameName.TIMEGUESSR, new String[] {
                    "{\"prompt\":\"Et norsk universitet åpner offisielt, med teknologi og realfag som sentral identitet.\",\"year\":1910,\"place\":\"Trondheim\",\"options\":[\"Bergen\",\"Oslo\",\"Trondheim\",\"Tromsø\"]}",
                    "{\"prompt\":\"Vinter-OL arrangeres i Norge med hoppbakker, maskoter og folkefest.\",\"year\":1994,\"place\":\"Lillehammer\",\"options\":[\"Lillehammer\",\"Oslo\",\"Bodø\",\"Stavanger\"]}",
                    "{\"prompt\":\"Et nytt operabygg åpner ved fjorden og blir raskt et landemerke.\",\"year\":2008,\"place\":\"Oslo\",\"options\":[\"Oslo\",\"Bergen\",\"Kristiansand\",\"Ålesund\"]}"
            }
    );

    private static String crosstunesPayload(String title, String[][] clues) {
        StringBuilder json = new StringBuilder();
        json.append("{\"dailySet\":20,\"dailySetVersion\":2,\"title\":\"")
                .append(jsonEscape(title))
                .append("\",\"rows\":11,\"cols\":12,\"clues\":[");
        for (int i = 0; i < clues.length; i++) {
            String[] clue = clues[i];
            if (i > 0) {
                json.append(",");
            }
            json.append("{\"n\":")
                    .append(clue[0])
                    .append(",\"dir\":\"")
                    .append(clue[1])
                    .append("\",\"row\":")
                    .append(clue[2])
                    .append(",\"col\":")
                    .append(clue[3])
                    .append(",\"answer\":\"")
                    .append(jsonEscape(clue[4]))
                    .append("\",\"clue\":\"")
                    .append(jsonEscape(clue[5]))
                    .append("\",\"trackId\":")
                    .append(clue.length > 6 ? clue[6] : "null")
                    .append(",\"previewUrl\":")
                    .append(clue.length > 7 ? "\"" + jsonEscape(clue[7]) + "\"" : "null")
                    .append("}");
        }
        json.append("]}");
        return json.toString();
    }

    private static String songlessPayload(String artist, String title, long trackId, String previewUrl) {
        return "{\"songlessVersion\":2,\"artist\":\"" + jsonEscape(artist) +
                "\",\"title\":\"" + jsonEscape(title) +
                "\",\"answer\":\"" + jsonEscape(title) +
                "\",\"trackId\":" + trackId +
                ",\"previewUrl\":\"" + jsonEscape(previewUrl) + "\"}";
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public NativePuzzleService(NativePuzzleRepository repository) {
        this.repository = repository;
    }

    public List<NativePuzzle> getAll(GameName gameName) {
        return repository.findByGameNameOrderByCreatedAtDesc(gameName);
    }

    @Transactional
    public NativePuzzle getDaily(GameName gameName) {
        LocalDate today = LocalDate.now();
        return repository.findByGameNameAndPuzzleDateAndIsDailyTrue(gameName, today)
                .map(puzzle -> refreshCrosstunesDailyIfNeeded(gameName, puzzle, today))
                .map(puzzle -> refreshSonglessDailyIfNeeded(gameName, puzzle, today))
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
        String[] payloads = DAILY_PAYLOADS.get(gameName);
        if (payloads == null || payloads.length == 0) {
            throw new IllegalArgumentException("Unsupported native game");
        }
        long dayIndex = date.toEpochDay() - LocalDate.of(2026, 1, 1).toEpochDay();
        int idx = (int) (((dayIndex % payloads.length) + payloads.length) % payloads.length);
        NativePuzzle puzzle = new NativePuzzle();
        puzzle.setGameName(gameName);
        puzzle.setCreatedBy("Glassburet");
        puzzle.setTitle(dailyTitle(gameName));
        puzzle.setIsDaily(true);
        puzzle.setPuzzleDate(date);
        puzzle.setPayloadJson(payloads[idx]);
        return repository.save(puzzle);
    }

    private NativePuzzle refreshCrosstunesDailyIfNeeded(GameName gameName, NativePuzzle puzzle, LocalDate date) {
        if (gameName != GameName.CROSSTUNES || puzzle.getPayloadJson() == null || puzzle.getPayloadJson().contains("\"dailySetVersion\":2")) {
            return puzzle;
        }
        String[] payloads = DAILY_PAYLOADS.get(gameName);
        long dayIndex = date.toEpochDay() - LocalDate.of(2026, 1, 1).toEpochDay();
        int idx = (int) (((dayIndex % payloads.length) + payloads.length) % payloads.length);
        puzzle.setPayloadJson(payloads[idx]);
        return repository.save(puzzle);
    }

    private NativePuzzle refreshSonglessDailyIfNeeded(GameName gameName, NativePuzzle puzzle, LocalDate date) {
        if (gameName != GameName.SONGLESS || puzzle.getPayloadJson() == null || puzzle.getPayloadJson().contains("\"songlessVersion\":2")) {
            return puzzle;
        }
        String[] payloads = DAILY_PAYLOADS.get(gameName);
        long dayIndex = date.toEpochDay() - LocalDate.of(2026, 1, 1).toEpochDay();
        int idx = (int) (((dayIndex % payloads.length) + payloads.length) % payloads.length);
        puzzle.setPayloadJson(payloads[idx]);
        return repository.save(puzzle);
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
            default -> "Daglig Spill";
        };
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
