package com.glassburet.service;

import com.glassburet.dto.NativePuzzleDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private final ObjectMapper objectMapper;

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
            crosstunesInterlockPayload("Kryssburet 01", "ALISON", "JOLENE", "HELLO", "ROSIE", "RHIANNON", "MANDY", "MARY", "ALONE"),
            crosstunesInterlockPayload("Kryssburet 02", "ALISON", "JOLENE", "TOXIC", "ANGIE", "RHIANNON", "GLORY", "GOLD", "OCEAN"),
            crosstunesInterlockPayload("Kryssburet 03", "ALISON", "JOLENE", "KARMA", "CREEP", "RHIANNON", "SUGAR", "STAY", "TOXIC"),
            crosstunesInterlockPayload("Kryssburet 04", "ALISON", "JOLENE", "CREEP", "GLORY", "RHIANNON", "LOVER", "LOLA", "OCEAN"),
            crosstunesInterlockPayload("Kryssburet 05", "ALISON", "JOLENE", "ANGIE", "TOXIC", "RHIANNON", "HELLO", "HURT", "UNDER"),
            crosstunesInterlockPayload("Kryssburet 06", "ALISON", "JOLENE", "HELLO", "KARMA", "RHIANNON", "DREAM", "DUSK", "UNDER"),
            crosstunesInterlockPayload("Kryssburet 07", "ALISON", "JOLENE", "GLORY", "CREEP", "RHIANNON", "MANDY", "MARY", "ANGIE"),
            crosstunesInterlockPayload("Kryssburet 08", "ALISON", "JOLENE", "TOXIC", "ROSIE", "RHIANNON", "ANGIE", "ABBA", "BILLS"),
            crosstunesInterlockPayload("Kryssburet 09", "ALISON", "JOLENE", "HELLO", "ANGIE", "RHIANNON", "CREEP", "CURE", "UNDER"),
            crosstunesInterlockPayload("Kryssburet 10", "ALISON", "JOLENE", "KARMA", "GLORY", "RHIANNON", "BILLS", "BETH", "EVERY"),
            crosstunesInterlockPayload("Kryssburet 11", "ALISON", "JOLENE", "CREEP", "TOXIC", "RHIANNON", "SUGAR", "SARA", "ALONE"),
            crosstunesInterlockPayload("Kryssburet 12", "ALISON", "JOLENE", "HELLO", "SARA", "RHIANNON", "INTRO", "IRIS", "ROSIE"),
            crosstunesInterlockPayload("Kryssburet 13", "ALISON", "JOLENE", "TOXIC", "ROSIE", "RHIANNON", "LOVER", "LUCY", "UNDER"),
            crosstunesInterlockPayload("Kryssburet 14", "ALISON", "JOLENE", "ANGIE", "HELLO", "RHIANNON", "GLORY", "GOLD", "OCEAN"),
            crosstunesInterlockPayload("Kryssburet 15", "ALISON", "JOLENE", "GLORY", "CREEP", "RHIANNON", "SUGAR", "STAY", "TOXIC"),
            crosstunesInterlockPayload("Kryssburet 16", "ALISON", "JOLENE", "KARMA", "ANGIE", "RHIANNON", "LOVER", "LOLA", "OCEAN"),
            crosstunesInterlockPayload("Kryssburet 17", "ALISON", "JOLENE", "TOXIC", "SARA", "RHIANNON", "HELLO", "HURT", "UNDER"),
            crosstunesInterlockPayload("Kryssburet 18", "ALISON", "JOLENE", "HELLO", "ROSIE", "RHIANNON", "DREAM", "DUSK", "UNDER"),
            crosstunesInterlockPayload("Kryssburet 19", "ALISON", "JOLENE", "CREEP", "ANGIE", "RHIANNON", "ANGIE", "ABBA", "BILLS"),
            crosstunesInterlockPayload("Kryssburet 20", "ALISON", "JOLENE", "GLORY", "HELLO", "RHIANNON", "MANDY", "MARY", "ALONE")
    };

    private static final String[] SONGLESS_DAILY_PAYLOADS = new String[] {
            songlessPayload("a-ha", "TAKE ON ME", 380907765, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/f2/03/4f/f2034f41-707f-7111-bc63-e5d3cf7f2240/mzaf_17215043934336702540.plus.aac.p.m4a"),
            songlessPayload("Kygo", "FIRESTONE", 1713785280, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview126/v4/a8/f4/27/a8f42729-3fab-5b8e-9702-8a818d1b8fad/mzaf_15234497499095842632.plus.aac.p.m4a"),
            songlessPayload("AURORA", "RUNAWAY", 1640380713, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview122/v4/34/12/66/34126650-f244-46fc-a7c1-8969e7df765c/mzaf_16801053975927778101.plus.aac.p.m4a"),
            songlessPayload("Ed Sheeran", "SHAPE OF YOU", 1193701392, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/44/c7/4f/44c74f0d-72dc-6143-d4d0-ba14d661ca0d/mzaf_9566898362556366703.plus.aac.p.m4a"),
            songlessPayload("Billie Eilish", "BAD GUY", 1450695739, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/c3/87/1f/c3871f7e-3260-d615-1c66-5fdca2c3a48f/mzaf_10721331211699880949.plus.aac.p.m4a"),
            songlessPayload("The Weeknd", "BLINDING LIGHTS", 1488408568, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/17/b4/8f/17b48f9a-0b93-6bb8-fe1d-3a16623c2cfb/mzaf_9560252727299052414.plus.aac.p.m4a"),
            songlessPayload("Dua Lipa", "LEVITATING", 1538003843, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/59/dc/4d/59dc4dda-93ff-8f1c-c536-f005f6ea6af5/mzaf_3066686759813252385.plus.aac.p.m4a"),
            songlessPayload("Harry Styles", "WATERMELON SUGAR", 1485802967, "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/16/86/f5/1686f50d-8b77-7e32-85f7-5f0e804d68fe/mzaf_14195633304344507287.plus.aac.p.m4a"),
            songlessPayload("Queen", "BOHEMIAN RHAPSODY", 0, null),
            songlessPayload("ABBA", "DANCING QUEEN", 0, null),
            songlessPayload("Toto", "AFRICA", 0, null),
            songlessPayload("Oasis", "WONDERWALL", 0, null),
            songlessPayload("Nirvana", "SMELLS LIKE TEEN SPIRIT", 0, null),
            songlessPayload("The Beatles", "HEY JUDE", 0, null),
            songlessPayload("Michael Jackson", "BILLIE JEAN", 0, null),
            songlessPayload("Fleetwood Mac", "DREAMS", 0, null),
            songlessPayload("Lady Gaga", "BAD ROMANCE", 0, null),
            songlessPayload("Coldplay", "VIVA LA VIDA", 0, null),
            songlessPayload("Rihanna", "UMBRELLA", 0, null),
            songlessPayload("Adele", "HELLO", 0, null)
    };

    private static final Map<GameName, String[]> DAILY_PAYLOADS = Map.of(
            GameName.MORE_OR_LESS, new String[] {
                    moreLessPayload("Hvilket land har flest innbyggere?","Norge","Danmark","right","Hvilket fjell er høyest?","Galdhøpiggen","Gaustatoppen","left","Hvilken by ligger lengst nord?","Trondheim","Bergen","left","Hva tar vanligvis lengst tid?","Koke pasta","Brygge espresso","left","Hvilket tall er høyest?","2^10","10^2","left"),
                    moreLessPayload("Hva har flest tangenter?","Piano","Datatastatur","left","Hvilket hav er størst?","Atlanterhavet","Stillehavet","right","Hvilket språk har flest bokstaver her?","JavaScript","Go","left","Hva veier mest?","En liter vann","Et vanlig eple","left","Hva er eldst?","Java","Kotlin","left"),
                    moreLessPayload("Hvilken planet er størst?","Jupiter","Mars","left","Hva er lengst?","Glomma","Akerselva","left","Hvilket dyr er raskest?","Gepard","Elg","left","Hva kom først?","HTML","CSS","left","Hva er høyest?","Mount Everest","K2","left"),
                    moreLessPayload("Hva har størst areal?","Sverige","Norge","left","Hva har flest minutter?","Et døgn","En arbeidsdag","left","Hva er tyngst?","Gull","Aluminium","left","Hva er nyest?","HTTP/3","HTTP/2","left","Hva er lengst unna jorda?","Månen","ISS","left"),
                    moreLessPayload("Hva har flest bokstaver?","PostgreSQL","Redis","left","Hva er varmest?","Solens overflate","Kokende vann","left","Hva er høyest?","Galdhøpiggen","Snøhetta","left","Hva er størst?","Mjøsa","Tyrifjorden","left","Hva tar lengst?","Maraton","100 meter","left"),
                    moreLessPayload("Hvilken by har flest innbyggere?","Tokyo","Oslo","left","Hva er eldst?","Python","Rust","left","Hva er dypest?","Marianergropen","Nordsjøen","left","Hva er størst?","Afrika","Europa","left","Hva har flest bein?","Edderkopp","Maur","left"),
                    moreLessPayload("Hva er raskest?","Lys","Lyd","left","Hva er lengst?","Nilen","Rhinen","left","Hva er størst?","Saturn","Merkur","left","Hva er eldre?","Linux","Android","left","Hva er tyngst?","Elefant","Hest","left"),
                    moreLessPayload("Hva har flest dager?","Skuddår","Vanlig år","left","Hva er størst?","Stillehavet","Indiahavet","left","Hva er lengst?","Tour de France","Birkebeinerrennet","left","Hva er nyest?","Vue 3","Vue 2","left","Hva er høyest?","Burj Khalifa","Eiffeltårnet","left"),
                    moreLessPayload("Hva veier mest?","En liter kvikksølv","En liter vann","left","Hva er størst?","Canada","Tyskland","left","Hva er raskest?","Fly","Tog","left","Hva er lengst?","Amazonas","Seinen","left","Hva er eldst?","C","Java","left"),
                    moreLessPayload("Hva er størst?","Månen","Pluto","left","Hva tar lengst tid?","Koke poteter","Lage kaffe","left","Hva har flest spillere?","Fotballag","Basketlag","left","Hva er høyest?","Preikestolen","Operataket","left","Hva er nyest?","ChatGPT","Wikipedia","left"),
                    moreLessPayload("Hva har flest kalorier?","Sjokolade","Gulrot","left","Hva er lengst?","E6","Ring 3","left","Hva er størst?","Oslofjorden","Nidelva","left","Hva er eldre?","Git","GitHub","left","Hva er raskest?","Fiber","ADSL","left"),
                    moreLessPayload("Hva er størst?","Sola","Jorda","left","Hva er lengst?","Et semester","En uke","left","Hva er tyngst?","Laptop","Mobil","left","Hva er eldre?","SMS","Snapchat","left","Hva har flest kort?","Kortstokk","Uno-hånd","left"),
                    moreLessPayload("Hva er høyest?","Kilimanjaro","Ben Nevis","left","Hva er størst?","Brasil","Norge","left","Hva er raskest?","5G","3G","left","Hva er nyest?","iPhone","iPod","left","Hva er lengst?","Atlanterhavet","Oslofjorden","left"),
                    moreLessPayload("Hva har flest seter?","Buss","Personbil","left","Hva er størst?","Hval","Delfin","left","Hva er eldre?","Radio","TV","left","Hva er høyest?","Rundetårn","Eiffeltårnet","right","Hva er raskest?","T-bane","Sykkel","left"),
                    moreLessPayload("Hva tar lengst tid?","Mastergrad","Bachelorgrad","left","Hva er størst?","Postgres","SQLite","left","Hva er eldre?","YouTube","TikTok","left","Hva er høyest?","Mount Fuji","Holmenkollen","left","Hva er raskest?","SSD","HDD","left"),
                    moreLessPayload("Hva er størst?","Asia","Australia","left","Hva er lengst?","Donau","Akerselva","left","Hva har flest liter?","Badekar","Kaffekopp","left","Hva er nyest?","Java 21","Java 8","left","Hva er tyngst?","Stål","Tre","left"),
                    moreLessPayload("Hva er raskest?","Høyhastighetstog","Trikk","left","Hva er størst?","Bergen","Bodø","left","Hva er eldre?","Facebook","Instagram","left","Hva er lengst?","Sommerferie","Helg","left","Hva er høyest?","Jupiter","Jorda","left"),
                    moreLessPayload("Hva har flest mennesker?","India","Island","left","Hva er størst?","Sahara","Hardangervidda","left","Hva er raskest?","Rakett","Bil","left","Hva er eldre?","Unix","Windows","left","Hva er lengst?","E39","Karl Johan","left"),
                    moreLessPayload("Hva er tyngst?","En sekk sement","En melkekartong","left","Hva er størst?","Trondheim","Molde","left","Hva er raskest?","Thunderbolt","USB 2","left","Hva er nyest?","IPv6","IPv4","left","Hva varer lengst?","Døgn","Time","left"),
                    moreLessPayload("Hva er høyest?","K2","Mont Blanc","left","Hva er størst?","Nord-Amerika","Grønland","left","Hva er eldre?","BASIC","Python","left","Hva er raskest?","Lyn","Regn","left","Hva er lengst?","Nordlandsbanen","Fløibanen","left")
            },
            GameName.SONGLESS, SONGLESS_DAILY_PAYLOADS,
            GameName.CROSSTUNES, CROSSTUNES_DAILY_PAYLOADS,
            GameName.TIMEGUESSR, new String[] {
                    timeguessrPayload(
                            "Plasser barrikadene på kartet og gjett året.",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Barricades_at_the_Brandenburg_Gate_1961.jpg",
                            52.5163, 13.3777, 1961, "Brandenburger Tor, Berlin",
                            "U.S. National Archives / Public domain"
                    ),
                    timeguessrPayload(
                            "Solstrålene faller inn i en kjent terminal. Hvor er vi?",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Grand_Central_rays_of_light.jpg",
                            40.7527, -73.9772, 1941, "Grand Central Terminal, New York",
                            "New York City Municipal Archives / Public domain"
                    ),
                    timeguessrPayload(
                            "Dette tårnet lyser under en verdensutstilling.",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Eiffel_Tower_illuminated_at_Paris_Exposition,_1900_LCCN2002699778.jpg",
                            48.8584, 2.2945, 1900, "Eiffeltårnet, Paris",
                            "Library of Congress / Public domain"
                    ),
                    timeguessrPayload(
                            "En historisk romferd starter herfra.",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Launch_of_Apollo_11_(S69-40640).jpg",
                            28.6083, -80.6041, 1969, "Launch Complex 39A, Kennedy Space Center",
                            "NASA / Public domain"
                    ),
                    timeguessrPayload(
                            "Et luftskip brenner ved landingsplassen. Sett pinnen nær hendelsen.",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Hindenburg_disaster,_1937.jpg",
                            40.0333, -74.3533, 1937, "Lakehurst, New Jersey",
                            "Murray Becker / Associated Press / Public domain"
                    ),
                    timeguessrPayload(
                            "En bro vokser frem over sundet. Hvor og når?",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Construction_of_the_Golden_Gate_Bridge_3c22793u.jpg",
                            37.8199, -122.4783, 1934, "Golden Gate Bridge, San Francisco",
                            "Library of Congress / Public domain"
                    ),
                    timeguessrPayload(
                            "Plasser barrikadene på kartet og gjett året.",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Barricades_at_the_Brandenburg_Gate_1961.jpg",
                            52.5163, 13.3777, 1961, "Brandenburger Tor, Berlin",
                            "U.S. National Archives / Public domain"
                    ),
                    timeguessrPayload(
                            "Solstrålene faller inn i en kjent terminal. Hvor er vi?",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Grand_Central_rays_of_light.jpg",
                            40.7527, -73.9772, 1941, "Grand Central Terminal, New York",
                            "New York City Municipal Archives / Public domain"
                    ),
                    timeguessrPayload(
                            "Dette tårnet lyser under en verdensutstilling.",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Eiffel_Tower_illuminated_at_Paris_Exposition,_1900_LCCN2002699778.jpg",
                            48.8584, 2.2945, 1900, "Eiffeltårnet, Paris",
                            "Library of Congress / Public domain"
                    ),
                    timeguessrPayload(
                            "En historisk romferd starter herfra.",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Launch_of_Apollo_11_(S69-40640).jpg",
                            28.6083, -80.6041, 1969, "Launch Complex 39A, Kennedy Space Center",
                            "NASA / Public domain"
                    ),
                    timeguessrPayload(
                            "Et luftskip brenner ved landingsplassen. Sett pinnen nær hendelsen.",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Hindenburg_disaster,_1937.jpg",
                            40.0333, -74.3533, 1937, "Lakehurst, New Jersey",
                            "Murray Becker / Associated Press / Public domain"
                    ),
                    timeguessrPayload(
                            "En bro vokser frem over sundet. Hvor og når?",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Construction_of_the_Golden_Gate_Bridge_3c22793u.jpg",
                            37.8199, -122.4783, 1934, "Golden Gate Bridge, San Francisco",
                            "Library of Congress / Public domain"
                    ),
                    timeguessrPayload(
                            "Plasser barrikadene på kartet og gjett året.",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Barricades_at_the_Brandenburg_Gate_1961.jpg",
                            52.5163, 13.3777, 1961, "Brandenburger Tor, Berlin",
                            "U.S. National Archives / Public domain"
                    ),
                    timeguessrPayload(
                            "Solstrålene faller inn i en kjent terminal. Hvor er vi?",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Grand_Central_rays_of_light.jpg",
                            40.7527, -73.9772, 1941, "Grand Central Terminal, New York",
                            "New York City Municipal Archives / Public domain"
                    ),
                    timeguessrPayload(
                            "Dette tårnet lyser under en verdensutstilling.",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Eiffel_Tower_illuminated_at_Paris_Exposition,_1900_LCCN2002699778.jpg",
                            48.8584, 2.2945, 1900, "Eiffeltårnet, Paris",
                            "Library of Congress / Public domain"
                    ),
                    timeguessrPayload(
                            "En historisk romferd starter herfra.",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Launch_of_Apollo_11_(S69-40640).jpg",
                            28.6083, -80.6041, 1969, "Launch Complex 39A, Kennedy Space Center",
                            "NASA / Public domain"
                    ),
                    timeguessrPayload(
                            "Et luftskip brenner ved landingsplassen. Sett pinnen nær hendelsen.",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Hindenburg_disaster,_1937.jpg",
                            40.0333, -74.3533, 1937, "Lakehurst, New Jersey",
                            "Murray Becker / Associated Press / Public domain"
                    ),
                    timeguessrPayload(
                            "En bro vokser frem over sundet. Hvor og når?",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Construction_of_the_Golden_Gate_Bridge_3c22793u.jpg",
                            37.8199, -122.4783, 1934, "Golden Gate Bridge, San Francisco",
                            "Library of Congress / Public domain"
                    ),
                    timeguessrPayload(
                            "Plasser barrikadene på kartet og gjett året.",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Barricades_at_the_Brandenburg_Gate_1961.jpg",
                            52.5163, 13.3777, 1961, "Brandenburger Tor, Berlin",
                            "U.S. National Archives / Public domain"
                    ),
                    timeguessrPayload(
                            "Solstrålene faller inn i en kjent terminal. Hvor er vi?",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Grand_Central_rays_of_light.jpg",
                            40.7527, -73.9772, 1941, "Grand Central Terminal, New York",
                            "New York City Municipal Archives / Public domain"
                    )
            }
    );

    private static String moreLessPayload(String... values) {
        StringBuilder json = new StringBuilder("{\"rounds\":[");
        for (int i = 0; i < values.length; i += 4) {
            if (i > 0) {
                json.append(",");
            }
            json.append("{\"metric\":\"").append(jsonEscape(values[i]))
                    .append("\",\"left\":\"").append(jsonEscape(values[i + 1]))
                    .append("\",\"right\":\"").append(jsonEscape(values[i + 2]))
                    .append("\",\"answer\":\"").append(jsonEscape(values[i + 3]))
                    .append("\"}");
        }
        json.append("]}");
        return json.toString();
    }

    private static String crosstunesPayload(String title, String[][] clues) {
        StringBuilder json = new StringBuilder();
        json.append("{\"dailySet\":20,\"dailySetVersion\":4,\"title\":\"")
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

    private static String crosstunesInterlockPayload(String title, String... answers) {
        return crosstunesPayload(title, new String[][] {
                {"1", "D", "0", "5", answers[0], "Song title"},
                {"2", "A", "1", "3", answers[1], "Song title"},
                {"3", "D", "2", "8", answers[2], "Song title"},
                {"4", "A", "3", "3", answers[3], "Song title"},
                {"5", "A", "5", "0", answers[4], "Song title"},
                {"6", "A", "7", "3", answers[5], "Song title"},
                {"7", "D", "7", "3", answers[6], "Song title"},
                {"8", "A", "8", "3", answers[7], "Song title"}
        });
    }

    private static String crosstunesRowsPayload(String title, String[][] clues) {
        int cols = 0;
        for (String[] clue : clues) {
            cols = Math.max(cols, clue[0].length());
        }
        StringBuilder json = new StringBuilder();
        json.append("{\"dailySet\":20,\"dailySetVersion\":3,\"title\":\"")
                .append(jsonEscape(title))
                .append("\",\"rows\":")
                .append(Math.max(7, clues.length * 2 - 1))
                .append(",\"cols\":")
                .append(Math.max(7, cols))
                .append(",\"clues\":[");
        for (int i = 0; i < clues.length; i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("{\"n\":")
                    .append(i + 1)
                    .append(",\"dir\":\"A\",\"row\":")
                    .append(i * 2)
                    .append(",\"col\":0,\"answer\":\"")
                    .append(jsonEscape(clues[i][0]))
                    .append("\",\"clue\":\"")
                    .append(jsonEscape(clues[i][1]))
                    .append("\",\"trackId\":null,\"previewUrl\":null}");
        }
        json.append("]}");
        return json.toString();
    }

    private static String songlessPayload(String artist, String title, long trackId, String previewUrl) {
        return "{\"songlessVersion\":2,\"artist\":\"" + jsonEscape(artist) +
                "\",\"title\":\"" + jsonEscape(title) +
                "\",\"answer\":\"" + jsonEscape(title) +
                "\",\"trackId\":" + trackId +
                ",\"previewUrl\":" + (previewUrl == null || previewUrl.isBlank() ? "null" : "\"" + jsonEscape(previewUrl) + "\"") + "}";
    }

    private static String timeguessrPayload(String prompt, String imageUrl, double lat, double lng, int year, String locationName, String attribution) {
        return "{\"timeguessrVersion\":3,\"prompt\":\"" + jsonEscape(prompt) +
                "\",\"imageUrl\":\"" + jsonEscape(imageUrl) +
                "\",\"lat\":" + lat +
                ",\"lng\":" + lng +
                ",\"year\":" + year +
                ",\"locationName\":\"" + jsonEscape(locationName) +
                "\",\"attribution\":\"" + jsonEscape(attribution) + "\"}";
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public NativePuzzleService(NativePuzzleRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
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
        if (gameName != GameName.CROSSTUNES || puzzle.getPayloadJson() == null || puzzle.getPayloadJson().contains("\"dailySetVersion\":4")) {
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

    private NativePuzzle refreshTimeguessrDailyIfNeeded(GameName gameName, NativePuzzle puzzle, LocalDate date) {
        if (gameName != GameName.TIMEGUESSR || puzzle.getPayloadJson() == null || puzzle.getPayloadJson().contains("\"timeguessrVersion\":3")) {
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
                root.remove("year");
                root.remove("place");
                root.remove("lat");
                root.remove("lng");
                root.remove("locationName");
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
        int year = payload.path("year").asInt();
        double lat = payload.path("lat").asDouble();
        double lng = payload.path("lng").asDouble();
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
                "locationName", payload.path("locationName").asText(payload.path("place").asText(""))
        );
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
