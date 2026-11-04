package com.furbo.service.matching;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "furbo.matching")
public class FurboMatchingProperties {
    private int teamMapMinVotes = 3;
    private double teamMapMinShare = 0.6;
    private int maxReviewCandidates = 3;
    private double autoLinkMinScore = 0.90;
    private double reviewMinScore = 0.55;
    private double minVotes = 3;
    private double minShare = 0.6;
    private double autoThreshold = 0.96;
    private double reviewThreshold = 0.90;
    private double margin = 0.05;
}
