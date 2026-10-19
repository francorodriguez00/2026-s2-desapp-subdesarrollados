package com.furbo.adapter.dto.whoscored;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhoScoredResponseDTO {
    private List<WhoScoredPlayerStatsDTO> playerTableStats;
    private PagingDTO paging;
}
