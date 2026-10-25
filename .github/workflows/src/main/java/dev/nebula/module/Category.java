package dev.nebula.module;

public enum Category {
    RENDER("Render"),
    MISC("Misc");

    public final String title;

    Category(String title) {
        this.title = title;
    }
}
