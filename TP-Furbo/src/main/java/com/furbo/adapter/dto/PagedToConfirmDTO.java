package com.furbo.adapter.dto;

import java.util.List;

public record PagedToConfirmDTO(
    List<ToConfirmItemDTO> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
