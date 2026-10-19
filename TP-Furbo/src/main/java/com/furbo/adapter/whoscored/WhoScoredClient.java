package com.furbo.adapter.whoscored;

import com.furbo.adapter.dto.whoscored.WhoScoredResponseDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WhoScoredClient {

    private final RestTemplate restTemplate;

    public WhoScoredClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public WhoScoredResponseDTO fetchStats(String url) {
        return restTemplate.getForObject(url, WhoScoredResponseDTO.class);
    }
}
