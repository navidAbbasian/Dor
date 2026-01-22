package com.example.dor.models;

import java.util.List;

/**
 * Model class representing a word category
 */
public class Category {
    private String id;
    private String name;
    private String emoji;
    private List<Word> words;
    private boolean selected;

    public Category() {
    }

    public Category(String id, String name, String emoji) {
        this.id = id;
        this.name = name;
        this.emoji = emoji;
        this.selected = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public List<Word> getWords() {
        return words;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getWordCount() {
        return words != null ? words.size() : 0;
    }

    public String getDisplayName() {
        return emoji + " " + name;
    }
}
