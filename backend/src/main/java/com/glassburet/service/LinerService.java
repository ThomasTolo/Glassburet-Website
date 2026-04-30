package com.glassburet.service;

import com.glassburet.dto.LinerDto;
import com.glassburet.model.Liner;
import com.glassburet.repository.LinerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

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
    public Liner create(LinerDto dto) {
        Liner liner = new Liner();
        int nextNumber = linerRepository.findTopByOrderByNumberDesc().map(Liner::getNumber).orElse(0) + 1;
        liner.setNumber(nextNumber);
        liner.setText(dto.getText());
        liner.setAuthor(dto.getAuthor());
        liner.setSinceYear(dto.getSinceYear());
        return linerRepository.save(liner);
    }

    @Transactional
    public Liner update(@NonNull Long id, LinerDto dto) {
        Liner liner = linerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Liner not found: " + id));
        liner.setText(dto.getText());
        liner.setAuthor(dto.getAuthor());
        liner.setSinceYear(dto.getSinceYear());
        return linerRepository.save(liner);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        linerRepository.deleteById(id);
    }

    @Transactional
    public Liner incrementSaidCount(@NonNull Long id) {
        Liner liner = linerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Liner not found: " + id));
        liner.setSaidCount(liner.getSaidCount() + 1);
        return linerRepository.save(liner);
    }

    @Transactional
    public Liner toggleLike(@NonNull Long id, String memberName) {
        Liner liner = linerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Liner not found: " + id));
        if (liner.getLikes().contains(memberName)) {
            liner.getLikes().remove(memberName);
        } else {
            liner.getLikes().add(memberName);
        }
        return linerRepository.save(liner);
    }
}
