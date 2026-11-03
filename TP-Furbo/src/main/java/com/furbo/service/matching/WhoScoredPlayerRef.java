package com.furbo.service.matching;

import java.util.List;

public record WhoScoredPlayerRef(
    String externalId,
    String name,
    String firstName,
    String lastName,
    List<String> teamNames,
    List<Integer> tournamentIds,
    String positionText
) {}
