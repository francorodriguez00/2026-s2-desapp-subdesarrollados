package com.furbo.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furbo.adapter.dto.whoscored.WhoScoredResponseDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WhoScoredDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testDeserialization() throws Exception {
        String json = "{\n" +
                "  \"playerTableStats\": [\n" +
                "    {\n" +
                "      \"playerId\": 12345,\n" +
                "      \"name\": \"John Doe\",\n" +
                "      \"teamName\": \"Test Team\",\n" +
                "      \"apps\": 10,\n" +
                "      \"minsPlayed\": 800,\n" +
                "      \"goal\": 5,\n" +
                "      \"assistTotal\": 2,\n" +
                "      \"yellowCard\": 1,\n" +
                "      \"redCard\": 0,\n" +
                "      \"shotsPerGame\": 2.5,\n" +
                "      \"passSuccess\": 85.5,\n" +
                "      \"aerialWonPerGame\": 1.2,\n" +
                "      \"manOfTheMatch\": 1,\n" +
                "      \"rating\": 7.5\n" +
                "    }\n" +
                "  ],\n" +
                "  \"paging\": {\n" +
                "    \"currentPage\": 1,\n" +
                "    \"totalPages\": 5,\n" +
                "    \"resultsPerPage\": 10,\n" +
                "    \"totalResults\": 50\n" +
                "  }\n" +
                "}";

        WhoScoredResponseDTO response = objectMapper.readValue(json, WhoScoredResponseDTO.class);

        assertNotNull(response);
        assertNotNull(response.getPlayerTableStats());
        assertEquals(1, response.getPlayerTableStats().size());
        assertEquals("John Doe", response.getPlayerTableStats().get(0).getName());
        assertEquals(7.5, response.getPlayerTableStats().get(0).getRating());
        assertEquals(5, response.getPaging().getTotalPages());
    }
}
