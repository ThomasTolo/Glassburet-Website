package com.glassburet.repository;

import com.glassburet.model.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.time.LocalDateTime;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    Optional<Quote> findFirstByFeaturedTrue();

    @Query("SELECT q FROM Quote q ORDER BY RAND()")
    Optional<Quote> findRandom();

    Optional<Quote> findTopByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    Optional<Quote> findTopByOrderByCreatedAtDesc();

    long countByFeaturedTrue();
}
