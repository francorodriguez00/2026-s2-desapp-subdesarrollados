package com.furbo.service.valuation;

public enum PositionGroup {
    FW, MF, DF, GK;

    public static PositionGroup fromPositionText(String positionText) {
        if (positionText == null) return null;
        return switch (positionText) {
            case "Forward" -> FW;
            case "Midfielder" -> MF;
            case "Defender" -> DF;
            case "Goalkeeper" -> GK;
            default -> null;
        };
    }
}
