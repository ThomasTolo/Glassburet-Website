package com.glassburet.dto;

import com.glassburet.model.Score;

public class ScoreSubmissionResult {
    private final Score score;
    private final boolean created;

    public ScoreSubmissionResult(Score score, boolean created) {
        this.score = score;
        this.created = created;
    }

    public Score getScore() { return score; }
    public boolean isCreated() { return created; }
}
