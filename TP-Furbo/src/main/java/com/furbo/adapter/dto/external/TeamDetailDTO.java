package com.furbo.adapter.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamDetailDTO {
    private String name;
    private List<PlayerDTO> squad;
}
