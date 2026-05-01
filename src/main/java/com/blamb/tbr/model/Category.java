package com.blamb.tbr.model;

public enum Category {
    BOOK("Book", "books", "library"),
    MOVIE("Movie", "movies", "cinema"),
    MUSIC("Music", "music", "disco");

    private final String displayName;
    private final String slug;
    private final String theme;

    Category(String displayName, String slug, String theme) {
        this.displayName = displayName;
        this.slug = slug;
        this.theme = theme;
    }

    public String getDisplayName() { return displayName; }
    public String getSlug() { return slug; }
    public String getTheme() { return theme; }
}
