package com.glassburet.dto;

import com.glassburet.model.GameName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ScoreSubmitDto {

    @NotBlank
    private String memberName;

    @NotNull
    private GameName gameName;

    private int scoreValue;

    @NotNull
    private LocalDate gameDate;

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public GameName getGameName() { return gameName; }
    public void setGameName(GameName gameName) { this.gameName = gameName; }
    public int getScoreValue() { return scoreValue; }
    public void setScoreValue(int scoreValue) { this.scoreValue = scoreValue; }
    public LocalDate getGameDate() { return gameDate; }
    public void setGameDate(LocalDate gameDate) { this.gameDate = gameDate; }
}
