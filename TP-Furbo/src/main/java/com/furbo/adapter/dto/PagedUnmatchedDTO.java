package com.furbo.adapter.dto;

import java.util.List;

public record PagedUnmatchedDTO(
    List<UnmatchedPlayerDTO> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
