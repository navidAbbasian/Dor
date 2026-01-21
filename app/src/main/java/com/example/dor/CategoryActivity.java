package com.example.dor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dor.adapters.CategoryAdapter;
import com.example.dor.data.WordRepository;
import com.example.dor.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        recyclerView = findViewById(R.id.categoriesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get categories from repository
        categories = WordRepository.getInstance().getCategories();

        adapter = new CategoryAdapter(categories, this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.nextButton).setOnClickListener(v -> onNextClicked());
    }

    @Override
    public void onCategoryClick(Category category, int position) {
        category.setSelected(!category.isSelected());
        adapter.notifyItemChanged(position);
    }

    private void onNextClicked() {
        List<String> selectedCategories = new ArrayList<>();
        for (Category category : categories) {
            if (category.isSelected()) {
                selectedCategories.add(category.getId());
            }
        }

        if (selectedCategories.isEmpty()) {
            Toast.makeText(this, R.string.min_one_category, Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare words for game
        WordRepository.getInstance().prepareWordsForGame(selectedCategories);

        // Go to mode selection
        Intent intent = new Intent(this, ModeActivity.class);
        startActivity(intent);
    }
}
