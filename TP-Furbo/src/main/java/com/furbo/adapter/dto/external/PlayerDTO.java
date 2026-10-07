package com.furbo.adapter.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerDTO {
    private String name;
    private String position;
    
    @JsonProperty("nationality")
    private String nationality;
}
