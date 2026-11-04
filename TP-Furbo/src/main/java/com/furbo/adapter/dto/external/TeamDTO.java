package com.furbo.adapter.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamDTO {
    private Long id;
    private String name;
}
