package com.glassburet.repository;

import com.glassburet.model.Liner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinerRepository extends JpaRepository<Liner, Long> {
    List<Liner> findAllByOrderByNumberAsc();
}
