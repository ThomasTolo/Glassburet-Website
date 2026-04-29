package com.glassburet.service;

import com.glassburet.dto.ConnectionsPuzzleDto;
import com.glassburet.dto.ConnectionsPuzzleResponse;
import com.glassburet.model.ConnectionsPuzzle;
import com.glassburet.repository.ConnectionsPuzzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ConnectionsPuzzleService {

    private final ConnectionsPuzzleRepository repository;

    public ConnectionsPuzzleService(ConnectionsPuzzleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ConnectionsPuzzleResponse create(ConnectionsPuzzleDto dto) {
        List<ConnectionsPuzzleDto.GroupDto> groups = dto.getGroups();

        ConnectionsPuzzle puzzle = new ConnectionsPuzzle();
        puzzle.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "Anonym");
        puzzle.setGroup0Category(groups.get(0).getCategory());
        puzzle.setGroup0Words(String.join(",", groups.get(0).getWords()));
        puzzle.setGroup1Category(groups.get(1).getCategory());
        puzzle.setGroup1Words(String.join(",", groups.get(1).getWords()));
        puzzle.setGroup2Category(groups.get(2).getCategory());
        puzzle.setGroup2Words(String.join(",", groups.get(2).getWords()));
        puzzle.setGroup3Category(groups.get(3).getCategory());
        puzzle.setGroup3Words(String.join(",", groups.get(3).getWords()));

        return toResponse(repository.save(puzzle));
    }

    public Optional<ConnectionsPuzzleResponse> getLatest() {
        return repository.findTopByOrderByCreatedAtDesc().map(this::toResponse);
    }

    private ConnectionsPuzzleResponse toResponse(ConnectionsPuzzle p) {
        ConnectionsPuzzleResponse resp = new ConnectionsPuzzleResponse();
        resp.setId(p.getId());
        resp.setCreatedBy(p.getCreatedBy());
        resp.setCreatedAt(p.getCreatedAt());
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
}
