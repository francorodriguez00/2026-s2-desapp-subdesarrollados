package com.furbo.adapter.dto;

public record PlayerLinkSummaryDTO(
    long players,
    long playersWithLink,
    long playersWithoutLink,
    long wsPlayersTotal,
    long wsPlayersLinked,
    long wsPlayersUnlinked,
    long pendingReviews,
    long linksToConfirm
) {}
