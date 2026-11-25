package com.furbo.unit.service.valuation;

import com.furbo.service.valuation.PositionGroup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionGroupTest {

    @Test
    void mapsForwardPosition() {
        assertEquals(PositionGroup.FW, PositionGroup.fromPositionText("Forward"));
    }

    @Test
    void mapsMidfielderPosition() {
        assertEquals(PositionGroup.MF, PositionGroup.fromPositionText("Midfielder"));
    }

    @Test
    void mapsDefenderPosition() {
        assertEquals(PositionGroup.DF, PositionGroup.fromPositionText("Defender"));
    }

    @Test
    void mapsGoalkeeperPosition() {
        assertEquals(PositionGroup.GK, PositionGroup.fromPositionText("Goalkeeper"));
    }

    @Test
    void returnsNullForNullPosition() {
        assertNull(PositionGroup.fromPositionText(null));
    }

    @Test
    void returnsNullForUnknownPosition() {
        assertNull(PositionGroup.fromPositionText("Striker"));
    }

    @Test
    void doesNotAcceptDifferentCapitalization() {
        assertNull(PositionGroup.fromPositionText("forward"));
    }
}