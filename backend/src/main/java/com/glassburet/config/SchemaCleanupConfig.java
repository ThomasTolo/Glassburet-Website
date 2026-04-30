package com.glassburet.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
public class SchemaCleanupConfig {

    @Bean
    CommandLineRunner dropLegacyScoreConstraint(DataSource dataSource) {
        return args -> {
            try (Connection conn = dataSource.getConnection()) {
                // Drop old score unique constraint/index on game_date (replaced by puzzle_id)
                conn.createStatement().execute("""
                    DO $$
                    DECLARE
                        cname text;
                    BEGIN
                        SELECT c.conname INTO cname
                        FROM pg_constraint c
                        JOIN pg_class t ON t.oid = c.conrelid
                        JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY(c.conkey)
                        WHERE t.relname = 'scores'
                          AND c.contype = 'u'
                          AND a.attname = 'game_date'
                        LIMIT 1;
                        IF cname IS NOT NULL THEN
                            EXECUTE 'ALTER TABLE scores DROP CONSTRAINT ' || quote_ident(cname);
                        END IF;
                    END $$;
                """);
                conn.createStatement().execute("DROP INDEX IF EXISTS uq_scores_member_game_date");
                // Fix NULL is_daily values left by schema migration on existing rows
                conn.createStatement().execute(
                    "UPDATE connections_puzzles SET is_daily = false WHERE is_daily IS NULL");
                conn.createStatement().execute(
                    "UPDATE wordle_puzzles SET is_daily = false WHERE is_daily IS NULL");
            } catch (Exception ignored) {}
        };
    }
}
