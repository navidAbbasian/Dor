package com.example.dor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dor.adapters.CategoryAdapter;
import com.example.dor.data.WordRepository;
import com.example.dor.models.Category;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categories;
    private MaterialCardView selectAllCard;
    private View selectAllCheckIcon;
    private TextView selectedCountText;
    private boolean allSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        recyclerView = findViewById(R.id.categoriesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        selectAllCard = findViewById(R.id.selectAllCard);
        selectAllCheckIcon = findViewById(R.id.selectAllCheckIcon);
        selectedCountText = findViewById(R.id.selectedCountText);

        // Get categories from repository
        categories = WordRepository.getInstance().getCategories();

        adapter = new CategoryAdapter(categories, this);
        recyclerView.setAdapter(adapter);

        // Select All click listener
        selectAllCard.setOnClickListener(v -> onSelectAllClicked());

        findViewById(R.id.nextButton).setOnClickListener(v -> onNextClicked());

        updateSelectedCount();
    }

    private void onSelectAllClicked() {
        allSelected = !allSelected;

        for (Category category : categories) {
            category.setSelected(allSelected);
        }
        adapter.notifyDataSetChanged();

        updateSelectAllUI();
        updateSelectedCount();
    }

    private void updateSelectAllUI() {
        if (allSelected) {
            selectAllCard.setStrokeColor(ContextCompat.getColor(this, R.color.primary));
            selectAllCard.setStrokeWidth(4);
            selectAllCheckIcon.setVisibility(View.VISIBLE);
        } else {
            selectAllCard.setStrokeColor(ContextCompat.getColor(this, R.color.primary));
            selectAllCard.setStrokeWidth(1);
            selectAllCheckIcon.setVisibility(View.GONE);
        }
    }

    private void updateSelectedCount() {
        int count = 0;
        for (Category category : categories) {
            if (category.isSelected()) {
                count++;
            }
        }
        selectedCountText.setText(count + " انتخاب شده");

        // Update allSelected state
        allSelected = (count == categories.size());
        updateSelectAllUI();
    }

    @Override
    public void onCategoryClick(Category category, int position) {
        category.setSelected(!category.isSelected());
        adapter.notifyItemChanged(position);
        updateSelectedCount();
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
