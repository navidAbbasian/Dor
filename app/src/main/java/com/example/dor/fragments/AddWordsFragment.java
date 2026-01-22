package com.example.dor.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dor.R;
import com.example.dor.data.WordRepository;
import com.example.dor.models.Category;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for adding custom words to the word bank
 */
public class AddWordsFragment extends Fragment {

    private TextInputEditText wordInput;
    private Spinner categorySpinner;
    private MaterialButton addWordButton;
    private RecyclerView myWordsRecyclerView;
    private LinearLayout emptyState;
    private WordAdapter adapter;
    private List<CustomWord> customWords;
    private SharedPreferences prefs;
    private Gson gson;

    private static final String PREFS_NAME = "custom_words_prefs";
    private static final String KEY_WORDS = "custom_words";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_words, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();

        // Initialize views
        wordInput = view.findViewById(R.id.wordInput);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        addWordButton = view.findViewById(R.id.addWordButton);
        myWordsRecyclerView = view.findViewById(R.id.myWordsRecyclerView);
        emptyState = view.findViewById(R.id.emptyState);

        // Setup category spinner
        setupCategorySpinner();

        // Setup RecyclerView
        setupRecyclerView();

        // Load saved words
        loadCustomWords();

        // Add button click
        addWordButton.setOnClickListener(v -> addWord());
    }

    private void setupCategorySpinner() {
        List<Category> categories = WordRepository.getInstance().getCategories();
        List<String> categoryNames = new ArrayList<>();

        for (Category category : categories) {
            categoryNames.add(category.getEmoji() + " " + category.getName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);
    }

    private void setupRecyclerView() {
        customWords = new ArrayList<>();
        adapter = new WordAdapter();
        myWordsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        myWordsRecyclerView.setAdapter(adapter);
    }

    private void loadCustomWords() {
        String json = prefs.getString(KEY_WORDS, "[]");
        Type type = new TypeToken<List<CustomWord>>() {}.getType();
        customWords.clear();
        List<CustomWord> loaded = gson.fromJson(json, type);
        if (loaded != null) {
            customWords.addAll(loaded);
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void saveCustomWords() {
        String json = gson.toJson(customWords);
        prefs.edit().putString(KEY_WORDS, json).apply();
    }

    private void addWord() {
        String word = wordInput.getText() != null ? wordInput.getText().toString().trim() : "";

        if (word.isEmpty()) {
            Toast.makeText(requireContext(), R.string.word_empty_error, Toast.LENGTH_SHORT).show();
            return;
        }

        List<Category> categories = WordRepository.getInstance().getCategories();
        int selectedIndex = categorySpinner.getSelectedItemPosition();
        if (selectedIndex < 0 || selectedIndex >= categories.size()) {
            return;
        }
        Category selectedCategory = categories.get(selectedIndex);

        CustomWord customWord = new CustomWord(word, selectedCategory.getId(), selectedCategory.getName());
        customWords.add(0, customWord);
        adapter.notifyItemInserted(0);
        myWordsRecyclerView.scrollToPosition(0);

        // Add to WordRepository for game use
        WordRepository.getInstance().addCustomWord(word, selectedCategory.getId());

        // Save to SharedPreferences
        saveCustomWords();

        // Clear input
        wordInput.setText("");

        Toast.makeText(requireContext(), R.string.word_added_success, Toast.LENGTH_SHORT).show();
        updateEmptyState();
    }

    private void deleteWord(int position) {
        if (position >= 0 && position < customWords.size()) {
            customWords.remove(position);
            adapter.notifyItemRemoved(position);
            saveCustomWords();
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (customWords.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            myWordsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            myWordsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Simple class to hold custom word data
     */
    public static class CustomWord {
        public String word;
        public String categoryId;
        public String categoryName;

        public CustomWord() {} // Required for Gson

        public CustomWord(String word, String categoryId, String categoryName) {
            this.word = word;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
        }
    }

    /**
     * Inner adapter class for custom words
     */
    private class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_custom_word, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CustomWord word = customWords.get(position);
            holder.wordText.setText(word.word);
            holder.categoryText.setText(word.categoryName);
            holder.deleteButton.setOnClickListener(v -> deleteWord(holder.getAdapterPosition()));
        }

        @Override
        public int getItemCount() {
            return customWords.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView wordText;
            final TextView categoryText;
            final ImageButton deleteButton;

            ViewHolder(View itemView) {
                super(itemView);
                wordText = itemView.findViewById(R.id.wordText);
                categoryText = itemView.findViewById(R.id.categoryText);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }
}

