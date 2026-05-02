package com.blamb.tbr.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * The single domain entity for the app: one row per TBR item.
 *
 * The annotations below ARE the database schema. There's no schema.sql —
 * Hibernate (pulled in transitively via spring-boot-starter-data-jpa) reads
 * these annotations on startup and CREATEs/ALTERs the tbr_item table to
 * match. That behavior is controlled by spring.jpa.hibernate.ddl-auto in
 * application.properties.
 *
 * The Jakarta Validation annotations (@NotBlank, @NotNull, @Size) are
 * enforced when @Valid appears on a controller parameter — they bubble
 * up as field errors that Thymeleaf shows next to the form input.
 */
@Entity
@Table(name = "tbr_item")
public class TbrItem {

    // Auto-incrementing primary key. IDENTITY = let the DB generate the ID
    // on INSERT; we don't try to set it ourselves.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 200)
    @Column(length = 200)
    private String creator;

    // EnumType.STRING stores the enum NAME ("BOOK") instead of its ordinal (0).
    // Why it matters: if someone reorders the enum later, ORDINAL would silently
    // corrupt every existing row. STRING is reorder-safe.
    @NotNull(message = "You must pick a category")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Category category;

    @Size(max = 1000)
    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime addedAt;

    @Column(nullable = false)
    private boolean completed = false;

    // @PrePersist runs once, just before Hibernate emits the INSERT. We use it
    // to stamp the creation time so the controller doesn't have to remember to
    // set it on every save.
    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
    }

    // --- Getters and setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
