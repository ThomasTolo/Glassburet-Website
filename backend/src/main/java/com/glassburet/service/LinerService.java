package com.glassburet.service;

import com.glassburet.model.Liner;
import com.glassburet.repository.LinerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LinerService {

    private final LinerRepository linerRepository;

    public LinerService(LinerRepository linerRepository) {
        this.linerRepository = linerRepository;
    }

    public List<Liner> findAll() {
        return linerRepository.findAllByOrderByNumberAsc();
    }

    @Transactional
    public Liner incrementSaidCount(Long id) {
        Liner liner = linerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Liner not found: " + id));
        liner.setSaidCount(liner.getSaidCount() + 1);
        return linerRepository.save(liner);
    }
}
