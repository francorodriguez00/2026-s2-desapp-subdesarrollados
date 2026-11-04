package com.furbo.adapter.dto;

import java.util.List;

public record PlayerLinkSummaryDTO(
    long players,
    long playersWithLink,
    long playersWithoutLink,
    long wsPlayersTotal,
    long wsPlayersLinked,
    long wsPlayersUnlinked
) {}
