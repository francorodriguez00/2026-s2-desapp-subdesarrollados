package com.furbo.adapter.dto;

import java.util.List;

public record PagedReviewItemsDTO(
    List<ReviewItemGroupDTO> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
