package me.eeshe.tempus.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public record Theme(
        String background,
        String surface,
        String text,
        String accent,
        String danger) {

    public static final String DEFAULT_BACKGROUND = "#101318";
    public static final String DEFAULT_SURFACE = "#161a22";
    public static final String DEFAULT_TEXT = "#e8eaf0";
    public static final String DEFAULT_ACCENT = "#3dd68c";
    public static final String DEFAULT_DANGER = "#f2555c";

    public static final Theme DEFAULT = new Theme(
            DEFAULT_BACKGROUND,
            DEFAULT_SURFACE,
            DEFAULT_TEXT,
            DEFAULT_ACCENT,
            DEFAULT_DANGER);
}
