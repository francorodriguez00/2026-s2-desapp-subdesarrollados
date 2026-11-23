package com.furbo.model.quoteStrategy;

import org.springframework.stereotype.Service;

@Service
public class ConsistencyDisciplineStrategy extends AbstractWeightedStrategy {
    @Override
    public String code() { return "CONSISTENCY_DISCIPLINE"; }
}
