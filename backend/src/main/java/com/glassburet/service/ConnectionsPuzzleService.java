package com.glassburet.service;

import com.glassburet.dto.ConnectionsPuzzleDto;
import com.glassburet.dto.ConnectionsPuzzleResponse;
import com.glassburet.model.ConnectionsPuzzle;
import com.glassburet.repository.ConnectionsPuzzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ConnectionsPuzzleService {

    private final ConnectionsPuzzleRepository repository;

    public ConnectionsPuzzleService(ConnectionsPuzzleRepository repository) {
        this.repository = repository;
    }

    private static final String[][] DAILY_PUZZLES = {
        {"Noe å drikke",        "KAFFE,TE,SAFT,BRUS",
         "Programmeringsspråk", "PYTHON,JAVA,SWIFT,RUBY",
         "Nordiske land",       "NORGE,SVERIGE,FINLAND,ISLAND",
         "Dyr i norsk natur",   "ÆLG,BJØRN,ULV,REV"},

        {"Norske byer",         "OSLO,BERGEN,TROMSØ,STAVANGER",
         "Instrumenter",        "GITAR,PIANO,FIOLIN,TROMMER",
         "Vanlige mattyper",    "PIZZA,TACO,SUSHI,BURGER",
         "Primærfarger",        "RØD,BLÅ,GRØNN,GUL"},

        {"Frontendrammeverk",   "REACT,VUE,ANGULAR,SVELTE",
         "Databaser",           "POSTGRES,MYSQL,MONGODB,REDIS",
         "Git-kommandoer",      "PUSH,PULL,MERGE,COMMIT",
         "Skyplattformer",      "AWS,AZURE,GCP,HEROKU"},

        {"Norske fjorder",      "SOGNE,HARDANGER,GEIRANGER,LYSE",
         "Årstider",            "VINTER,VÅR,SOMMER,HØST",
         "Planeter",            "MARS,VENUS,JUPITER,SATURN",
         "Norsk tradisjonskost","BRUNOST,LUTEFISK,RAKFISK,LEFSE"},

        {"Datastrukturer",      "LISTE,STAKK,KØ,TRE",
         "Agile begreper",      "SPRINT,BACKLOG,STANDUP,REVIEW",
         "Nettlesere",          "CHROME,FIREFOX,SAFARI,EDGE",
         "Operativsystemer",    "WINDOWS,LINUX,MACOS,ANDROID"},

        {"Norske fotballag",    "VIKING,BRANN,ROSENBORG,VÅLERENGA",
         "Treningsformer",      "YOGA,PILATES,CROSSFIT,SVØMMING",
         "Vintersport",         "LANGRENN,SLALOM,SKIFLYGING,SKISKYTING",
         "Norske idrettsstjerner","NORTHUG,KLÆBO,HAALAND,WARHOLM"},

        {"Rom i huset",         "KJØKKEN,STUE,SOVEROM,BAD",
         "Klesstykker",         "JAKKE,BUKSE,SKJORTE,GENSER",
         "Vær",                 "REGN,SNØ,TORDEN,SOL",
         "Norsk slang for bra", "KULT,DIGG,BALLA,FETT"},

        {"Glassburet-sider",    "GALLERI,POENG,HJEM,AKTIVITETER",
         "Frukt",               "EPLE,BANAN,APPELSIN,PÆRE",
         "Programmeringsbegreper","KLASSE,OBJEKT,METODE,LØKKE",
         "Norske artister",     "AURORA,SIGRID,YLVIS,ASTRID"},

        {"Kjøkkenredskaper",    "KNIV,GAFFEL,SKJE,STEKEPANNE",
         "Matematikkbegreper",  "ALGEBRA,GEOMETRI,KALKULUS,STATISTIKK",
         "Studiesteder i Norge","NTNU,UIO,UIB,UIS",
         "Nettverksprotokoller","HTTP,TCP,UDP,DNS"},

        {"Norsk natur",         "FJORD,FJELL,SKOG,ELVER",
         "Ukedager",            "MANDAG,TIRSDAG,ONSDAG,TORSDAG",
         "Farger på norsk flagg","RØD,BLÅ,HVIT,GUL",
         "Hobbyaktiviteter",    "GAMING,MALING,LØPING,LESING"},
    };

    @Transactional
    public ConnectionsPuzzleResponse create(ConnectionsPuzzleDto dto) {
        List<ConnectionsPuzzleDto.GroupDto> groups = dto.getGroups();

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

        long dayIndex = today.toEpochDay() - LocalDate.of(2026, 1, 1).toEpochDay();
        int idx = (int) (((dayIndex % DAILY_PUZZLES.length) + DAILY_PUZZLES.length) % DAILY_PUZZLES.length);
        String[] p = DAILY_PUZZLES[idx];

        ConnectionsPuzzle puzzle = new ConnectionsPuzzle();
        puzzle.setCreatedBy("Glassburet");
        puzzle.setTitle("Daglig Tankeburet");
        puzzle.setIsDaily(true);
        puzzle.setPuzzleDate(today);
        puzzle.setGroup0Category(p[0]);
        puzzle.setGroup0Words(p[1]);
        puzzle.setGroup1Category(p[2]);
        puzzle.setGroup1Words(p[3]);
        puzzle.setGroup2Category(p[4]);
        puzzle.setGroup2Words(p[5]);
        puzzle.setGroup3Category(p[6]);
        puzzle.setGroup3Words(p[7]);

        return toResponse(repository.save(puzzle));
    }

    public Optional<ConnectionsPuzzleResponse> getLatest() {
        return repository.findTopByOrderByCreatedAtDesc().map(this::toResponse);
    }

    public List<ConnectionsPuzzleResponse> getAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());
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
        List<ConnectionsPuzzleDto.GroupDto> groups = dto.getGroups();
        puzzle.setTitle(clean(dto.getTitle()));
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
