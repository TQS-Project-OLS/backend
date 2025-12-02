package com.example.OLSHEETS.data;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("MUSIC_SHEET")
public class MusicSheet extends Item {

    private String composer;
    private String instrumentation;
    private String category;
    private Float duration;

    public MusicSheet() {
    }

    public MusicSheet(String title, String category, String composer, Long ownerId) {
        this.setName(title);
        this.category = category;
        this.composer = composer;
        this.setOwnerId(ownerId.intValue());
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getInstrumentation() {
        return instrumentation;
    }

    public void setInstrumentation(String instrumentation) {
        this.instrumentation = instrumentation;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Float getDuration() {
        return duration;
    }

    public void setDuration(Float duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return getName();
    }

    public void setTitle(String title) {
        setName(title);
    }

    public void setOwnerId(Long ownerId) {
        setOwnerId(ownerId.intValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MusicSheet)) return false;
        if (!super.equals(o)) return false;

        MusicSheet that = (MusicSheet) o;

        if (composer != null ? !composer.equals(that.composer) : that.composer != null) return false;
        if (instrumentation != null ? !instrumentation.equals(that.instrumentation) : that.instrumentation != null)
            return false;
        if (category != null ? !category.equals(that.category) : that.category != null) return false;
        return duration != null ? duration.equals(that.duration) : that.duration == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (composer != null ? composer.hashCode() : 0);
        result = 31 * result + (instrumentation != null ? instrumentation.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        return result;
    }
}