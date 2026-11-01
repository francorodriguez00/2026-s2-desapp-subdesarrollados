package com.furbo.model;

import lombok.Data;
import java.util.Map;

@Data
public class StrategyConfigData {
    private Double baseValue;
    private Double scaleFactor;
    private Integer minMinutes;
    private Map<String, Map<String, Double>> ranges;
    private Map<String, Map<String, Double>> weights;
}
