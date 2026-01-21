package com.example.dor.models;

/**
 * Model class representing a word with its category and difficulty
 */
public class Word {
    private String text;
    private String category;
    private int difficulty; // 1 = easy, 2 = medium, 3 = hard

    public Word() {
    }

    public Word(String text, String category, int difficulty) {
        this.text = text;
        this.category = category;
        this.difficulty = difficulty;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
}
