package com.enums;

import java.awt.Color;

/**
 * Centralized palette used across the application tabs to ensure a consistent look & feel.
 */
public enum ThemeColor {
    PRIMARY(new Color(30, 144, 255)),
    PRIMARY_DARK(new Color(24, 100, 171)),
    SECONDARY(new Color(126, 87, 194)),
    ACCENT(new Color(236, 64, 122)),
    SUCCESS(new Color(76, 175, 80)),
    WARNING(new Color(255, 193, 7)),
    INFO(new Color(0, 188, 212)),
    LIGHT_BG(new Color(245, 248, 255)),
    CARD_BG(new Color(255, 255, 255)),
    TEXT_DARK(new Color(33, 33, 33)),
    TEXT_LIGHT(Color.WHITE);

    private final Color awtColor;

    ThemeColor(Color awtColor) {
        this.awtColor = awtColor;
    }

    public Color color() {
        return awtColor;
    }
}
