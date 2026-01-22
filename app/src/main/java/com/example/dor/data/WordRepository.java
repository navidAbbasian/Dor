package com.example.dor.data;

import android.content.Context;

import com.example.dor.models.Category;
import com.example.dor.models.Word;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository for managing words and categories
 */
public class WordRepository {
    private static WordRepository instance;
    private List<Category> categories;
    private List<Word> availableWords;
    private List<Word> usedWords;
    private int currentWordIndex;

    private WordRepository() {
        categories = new ArrayList<>();
        availableWords = new ArrayList<>();
        usedWords = new ArrayList<>();
        currentWordIndex = 0;
    }

    public static synchronized WordRepository getInstance() {
        if (instance == null) {
            instance = new WordRepository();
        }
        return instance;
    }

    public void loadCategories(Context context) {
        try {
            InputStream is = context.getAssets().open("words.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Category>>() {}.getType();
            categories = gson.fromJson(json, listType);
        } catch (IOException e) {
            e.printStackTrace();
            categories = getDefaultCategories();
        }
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void prepareWordsForGame(List<String> selectedCategoryIds) {
        availableWords.clear();
        usedWords.clear();
        currentWordIndex = 0;

        for (Category category : categories) {
            if (selectedCategoryIds.contains(category.getId()) && category.getWords() != null) {
                availableWords.addAll(category.getWords());
            }
        }

        // Shuffle words for randomness
        Collections.shuffle(availableWords);
    }

    public Word getNextWord() {
        if (availableWords.isEmpty()) {
            return null;
        }

        if (currentWordIndex >= availableWords.size()) {
            // Reset and reshuffle if we run out of words
            currentWordIndex = 0;
            Collections.shuffle(availableWords);
        }

        Word word = availableWords.get(currentWordIndex);
        currentWordIndex++;
        return word;
    }

    public Word skipWord() {
        return getNextWord();
    }

    public int getRemainingWordsCount() {
        return availableWords.size() - currentWordIndex;
    }

    public void reset() {
        availableWords.clear();
        usedWords.clear();
        currentWordIndex = 0;
    }

    /**
     * Add a custom word to a category
     */
    public void addCustomWord(String text, String categoryId) {
        Word customWord = new Word(text, categoryId, 1);

        // Find the category and add the word
        for (Category category : categories) {
            if (category.getId().equals(categoryId)) {
                if (category.getWords() == null) {
                    category.setWords(new ArrayList<>());
                }
                category.getWords().add(customWord);
                break;
            }
        }
    }

    private List<Category> getDefaultCategories() {
        List<Category> defaultCategories = new ArrayList<>();

        // Film & Series
        Category film = new Category("film", "فیلم و سریال", "🎬");
        List<Word> filmWords = new ArrayList<>();
        filmWords.add(new Word("تایتانیک", "film", 1));
        filmWords.add(new Word("شوالیه تاریکی", "film", 2));
        filmWords.add(new Word("پدرخوانده", "film", 2));
        filmWords.add(new Word("فرار از زندان", "film", 1));
        filmWords.add(new Word("بازی تاج و تخت", "film", 1));
        filmWords.add(new Word("جومونگ", "film", 1));
        filmWords.add(new Word("شهرزاد", "film", 1));
        filmWords.add(new Word("پایتخت", "film", 1));
        filmWords.add(new Word("جدایی نادر از سیمین", "film", 2));
        filmWords.add(new Word("مرد عنکبوتی", "film", 1));
        film.setWords(filmWords);
        defaultCategories.add(film);

        // Sports
        Category sports = new Category("sports", "ورزش", "⚽");
        List<Word> sportsWords = new ArrayList<>();
        sportsWords.add(new Word("فوتبال", "sports", 1));
        sportsWords.add(new Word("والیبال", "sports", 1));
        sportsWords.add(new Word("بسکتبال", "sports", 1));
        sportsWords.add(new Word("کشتی", "sports", 1));
        sportsWords.add(new Word("شنا", "sports", 1));
        sportsWords.add(new Word("تنیس", "sports", 1));
        sportsWords.add(new Word("بوکس", "sports", 1));
        sportsWords.add(new Word("دوچرخه‌سواری", "sports", 1));
        sportsWords.add(new Word("اسکی", "sports", 2));
        sportsWords.add(new Word("پرش با چتر", "sports", 2));
        sports.setWords(sportsWords);
        defaultCategories.add(sports);

        // Food
        Category food = new Category("food", "غذا و نوشیدنی", "🍕");
        List<Word> foodWords = new ArrayList<>();
        foodWords.add(new Word("پیتزا", "food", 1));
        foodWords.add(new Word("چلوکباب", "food", 1));
        foodWords.add(new Word("قرمه‌سبزی", "food", 1));
        foodWords.add(new Word("سوشی", "food", 2));
        foodWords.add(new Word("همبرگر", "food", 1));
        foodWords.add(new Word("فسنجان", "food", 1));
        foodWords.add(new Word("آش رشته", "food", 1));
        foodWords.add(new Word("دوغ", "food", 1));
        foodWords.add(new Word("چای", "food", 1));
        foodWords.add(new Word("بستنی", "food", 1));
        food.setWords(foodWords);
        defaultCategories.add(food);

        // Places
        Category places = new Category("places", "مکان و کشور", "🌍");
        List<Word> placesWords = new ArrayList<>();
        placesWords.add(new Word("برج ایفل", "places", 1));
        placesWords.add(new Word("دیوار چین", "places", 1));
        placesWords.add(new Word("برج میلاد", "places", 1));
        placesWords.add(new Word("تخت جمشید", "places", 1));
        placesWords.add(new Word("اهرام مصر", "places", 1));
        placesWords.add(new Word("تاج محل", "places", 2));
        placesWords.add(new Word("آبشار نیاگارا", "places", 2));
        placesWords.add(new Word("کلوسئوم", "places", 2));
        placesWords.add(new Word("برج پیزا", "places", 2));
        placesWords.add(new Word("سیدنی", "places", 2));
        places.setWords(placesWords);
        defaultCategories.add(places);

        // Famous People
        Category people = new Category("people", "شخصیت‌های معروف", "👤");
        List<Word> peopleWords = new ArrayList<>();
        peopleWords.add(new Word("مسی", "people", 1));
        peopleWords.add(new Word("رونالدو", "people", 1));
        peopleWords.add(new Word("محمدرضا گلزار", "people", 1));
        peopleWords.add(new Word("فردوسی", "people", 2));
        peopleWords.add(new Word("حافظ", "people", 1));
        peopleWords.add(new Word("اینشتین", "people", 1));
        peopleWords.add(new Word("لئوناردو داوینچی", "people", 2));
        peopleWords.add(new Word("مایکل جکسون", "people", 1));
        peopleWords.add(new Word("علی دایی", "people", 1));
        peopleWords.add(new Word("شجریان", "people", 2));
        people.setWords(peopleWords);
        defaultCategories.add(people);

        // Music
        Category music = new Category("music", "موسیقی", "🎵");
        List<Word> musicWords = new ArrayList<>();
        musicWords.add(new Word("گیتار", "music", 1));
        musicWords.add(new Word("پیانو", "music", 1));
        musicWords.add(new Word("ویولن", "music", 1));
        musicWords.add(new Word("درامز", "music", 1));
        musicWords.add(new Word("تار", "music", 2));
        musicWords.add(new Word("سنتور", "music", 2));
        musicWords.add(new Word("رپ", "music", 1));
        musicWords.add(new Word("پاپ", "music", 1));
        musicWords.add(new Word("راک", "music", 1));
        musicWords.add(new Word("کنسرت", "music", 1));
        music.setWords(musicWords);
        defaultCategories.add(music);

        // Books
        Category books = new Category("books", "کتاب", "📚");
        List<Word> booksWords = new ArrayList<>();
        booksWords.add(new Word("شاهنامه", "books", 1));
        booksWords.add(new Word("هری پاتر", "books", 1));
        booksWords.add(new Word("کیمیاگر", "books", 2));
        booksWords.add(new Word("شازده کوچولو", "books", 1));
        booksWords.add(new Word("بینوایان", "books", 2));
        booksWords.add(new Word("گلستان", "books", 2));
        booksWords.add(new Word("مثنوی", "books", 2));
        booksWords.add(new Word("ارباب حلقه‌ها", "books", 1));
        booksWords.add(new Word("جنگ و صلح", "books", 3));
        booksWords.add(new Word("صد سال تنهایی", "books", 3));
        books.setWords(booksWords);
        defaultCategories.add(books);

        // Animals
        Category animals = new Category("animals", "حیوانات", "🐾");
        List<Word> animalsWords = new ArrayList<>();
        animalsWords.add(new Word("شیر", "animals", 1));
        animalsWords.add(new Word("فیل", "animals", 1));
        animalsWords.add(new Word("زرافه", "animals", 1));
        animalsWords.add(new Word("دلفین", "animals", 1));
        animalsWords.add(new Word("پنگوئن", "animals", 1));
        animalsWords.add(new Word("کانگورو", "animals", 1));
        animalsWords.add(new Word("پلنگ", "animals", 1));
        animalsWords.add(new Word("عقاب", "animals", 1));
        animalsWords.add(new Word("کوسه", "animals", 1));
        animalsWords.add(new Word("اختاپوس", "animals", 2));
        animals.setWords(animalsWords);
        defaultCategories.add(animals);

        // Jobs
        Category jobs = new Category("jobs", "شغل و حرفه", "💼");
        List<Word> jobsWords = new ArrayList<>();
        jobsWords.add(new Word("دکتر", "jobs", 1));
        jobsWords.add(new Word("مهندس", "jobs", 1));
        jobsWords.add(new Word("معلم", "jobs", 1));
        jobsWords.add(new Word("آشپز", "jobs", 1));
        jobsWords.add(new Word("خلبان", "jobs", 1));
        jobsWords.add(new Word("آتش‌نشان", "jobs", 1));
        jobsWords.add(new Word("بازیگر", "jobs", 1));
        jobsWords.add(new Word("خواننده", "jobs", 1));
        jobsWords.add(new Word("فضانورد", "jobs", 2));
        jobsWords.add(new Word("جراح", "jobs", 2));
        jobs.setWords(jobsWords);
        defaultCategories.add(jobs);

        // General
        Category general = new Category("general", "عمومی", "🎭");
        List<Word> generalWords = new ArrayList<>();
        generalWords.add(new Word("عشق", "general", 1));
        generalWords.add(new Word("آزادی", "general", 2));
        generalWords.add(new Word("سفر", "general", 1));
        generalWords.add(new Word("جشن تولد", "general", 1));
        generalWords.add(new Word("عید نوروز", "general", 1));
        generalWords.add(new Word("یلدا", "general", 1));
        generalWords.add(new Word("کریسمس", "general", 1));
        generalWords.add(new Word("هالووین", "general", 2));
        generalWords.add(new Word("ماه عسل", "general", 2));
        generalWords.add(new Word("قایق", "general", 1));
        general.setWords(generalWords);
        defaultCategories.add(general);

        return defaultCategories;
    }
}
