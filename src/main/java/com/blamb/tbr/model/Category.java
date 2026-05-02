package com.blamb.tbr.model;

/**
 * The three TBR categories.
 *
 * Each value carries two helper strings:
 *   - displayName: human-readable label for UI text ("Book")
 *   - slug:        URL path segment used in redirects ("books")
 *
 * Keeping these on the enum (instead of computing them in templates or
 * controllers) means there's exactly one place to change them.
 */
public enum Category {
    BOOK("Book", "books"),
    MOVIE("Movie", "movies"),
    MUSIC("Music", "music");

    private final String displayName;
    private final String slug;

    Category(String displayName, String slug) {
        this.displayName = displayName;
        this.slug = slug;
    }

    public String getDisplayName() { return displayName; }
    public String getSlug() { return slug; }
}
