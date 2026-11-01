package com.furbo.service.valuation;

import org.springframework.stereotype.Service;

@Service
public class ConsistencyDisciplineStrategy extends AbstractWeightedStrategy {
    @Override
    public String code() { return "CONSISTENCY_DISCIPLINE"; }
}
