package com.glassburet.repository;

import com.glassburet.model.Liner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LinerRepository extends JpaRepository<Liner, Long> {
    List<Liner> findAllByOrderByNumberAsc();
    Optional<Liner> findTopByOrderByNumberDesc();
}
