package com.furbo.adapter.dto.whoscored;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhoScoredPlayerStatsDTO {
    @JsonProperty("playerId")
    private Long playerId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("teamName")
    private String teamName;
    
    @JsonProperty("apps")
    private Integer apps;
    
    @JsonProperty("minsPlayed")
    private Integer minsPlayed;
    
    @JsonProperty("goal")
    private Integer goal;
    
    @JsonProperty("assistTotal")
    private Integer assistTotal;
    
    @JsonProperty("yellowCard")
    private Integer yellowCard;
    
    @JsonProperty("redCard")
    private Integer redCard;
    
    @JsonProperty("shotsPerGame")
    private Double shotsPerGame;
    
    @JsonProperty("passSuccess")
    private Double passSuccess;
    
    @JsonProperty("aerialWonPerGame")
    private Double aerialWonPerGame;
    
    @JsonProperty("manOfTheMatch")
    private Integer manOfTheMatch;
    
    @JsonProperty("rating")
    private Double rating;
}
