package com.furbo.adapter.dto.whoscored;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagingDTO {
    private Integer currentPage;
    private Integer totalPages;
    private Integer resultsPerPage;
    private Integer totalResults;
}
