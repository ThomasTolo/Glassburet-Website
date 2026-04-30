package com.glassburet.service;

import com.glassburet.dto.NativePuzzleDto;
import com.glassburet.model.GameName;
import com.glassburet.model.NativePuzzle;
import com.glassburet.repository.NativePuzzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class NativePuzzleService {

    private final NativePuzzleRepository repository;

    private static final Map<GameName, String[]> DAILY_PAYLOADS = Map.of(
            GameName.MORE_OR_LESS, new String[] {
                    "{\"rounds\":[{\"metric\":\"Hvilket land har flest innbyggere?\",\"left\":\"Norge\",\"right\":\"Danmark\",\"answer\":\"right\"},{\"metric\":\"Hvilket fjell er høyest?\",\"left\":\"Galdhøpiggen\",\"right\":\"Gaustatoppen\",\"answer\":\"left\"},{\"metric\":\"Hvilken by ligger lengst nord?\",\"left\":\"Trondheim\",\"right\":\"Bergen\",\"answer\":\"left\"},{\"metric\":\"Hvilken hendelse er nyest?\",\"left\":\"HTTP/2 standardiseres\",\"right\":\"Vue lanseres\",\"answer\":\"left\"},{\"metric\":\"Hva tar vanligvis lengst tid?\",\"left\":\"Koke pasta\",\"right\":\"Brygge espresso\",\"answer\":\"left\"}]}",
                    "{\"rounds\":[{\"metric\":\"Hva har flest tangenter?\",\"left\":\"Piano\",\"right\":\"Datatastatur\",\"answer\":\"left\"},{\"metric\":\"Hvilket hav er størst?\",\"left\":\"Atlanterhavet\",\"right\":\"Stillehavet\",\"answer\":\"right\"},{\"metric\":\"Hvilket språk har flest bokstaver her?\",\"left\":\"JavaScript\",\"right\":\"Go\",\"answer\":\"left\"},{\"metric\":\"Hva veier mest?\",\"left\":\"En liter vann\",\"right\":\"Et vanlig eple\",\"answer\":\"left\"},{\"metric\":\"Hvilket tall er høyest?\",\"left\":\"2^10\",\"right\":\"10^2\",\"answer\":\"left\"}]}"
            },
            GameName.SONGLESS, new String[] {
                    "{\"answer\":\"NATTTOG\",\"clues\":[\"Reise etter solnedgang\",\"Rytmen går jevnt fremover\",\"Tittelen har to deler\"]}",
                    "{\"answer\":\"REGNDANS\",\"clues\":[\"Vær møter bevegelse\",\"Passer best med mørke skyer\",\"Tittelen slutter med dans\"]}",
                    "{\"answer\":\"LYS I VINDUET\",\"clues\":[\"Noe varmt i mørket\",\"Handler om å finne hjem\",\"Tre ord\"]}"
            },
            GameName.CROSSTUNES, new String[] {
                    "{\"clues\":[{\"prompt\":\"Holder takten\",\"answer\":\"TROMME\"},{\"prompt\":\"Seks strenger\",\"answer\":\"GITAR\"},{\"prompt\":\"Synges av flere samtidig\",\"answer\":\"KOR\"},{\"prompt\":\"Liten melodi som fester seg\",\"answer\":\"HOOK\"}]}",
                    "{\"clues\":[{\"prompt\":\"Rolig styrke i musikken\",\"answer\":\"PIANO\"},{\"prompt\":\"Raskere enn andante\",\"answer\":\"ALLEGRO\"},{\"prompt\":\"Musikk uten sang\",\"answer\":\"INSTRUMENTAL\"},{\"prompt\":\"Bygger opp mot refrenget\",\"answer\":\"VERS\"}]}"
            },
            GameName.TIMEGUESSR, new String[] {
                    "{\"prompt\":\"Et norsk universitet åpner offisielt, med teknologi og realfag som sentral identitet.\",\"year\":1910,\"place\":\"Trondheim\",\"options\":[\"Bergen\",\"Oslo\",\"Trondheim\",\"Tromsø\"]}",
                    "{\"prompt\":\"Vinter-OL arrangeres i Norge med hoppbakker, maskoter og folkefest.\",\"year\":1994,\"place\":\"Lillehammer\",\"options\":[\"Lillehammer\",\"Oslo\",\"Bodø\",\"Stavanger\"]}",
                    "{\"prompt\":\"Et nytt operabygg åpner ved fjorden og blir raskt et landemerke.\",\"year\":2008,\"place\":\"Oslo\",\"options\":[\"Oslo\",\"Bergen\",\"Kristiansand\",\"Ålesund\"]}"
            }
    );

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
                .orElseGet(() -> createDaily(gameName, today));
    }

    @Transactional
    public NativePuzzle create(GameName gameName, NativePuzzleDto dto) {
        NativePuzzle puzzle = new NativePuzzle();
        puzzle.setGameName(gameName);
        puzzle.setCreatedBy(dto.getCreatedBy() != null && !dto.getCreatedBy().isBlank() ? dto.getCreatedBy() : "Anonym");
        puzzle.setPayloadJson(dto.getPayloadJson());
        return repository.save(puzzle);
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
        puzzle.setIsDaily(true);
        puzzle.setPuzzleDate(date);
        puzzle.setPayloadJson(payloads[idx]);
        return repository.save(puzzle);
    }
}
