package com.example.dor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dor.R;
import com.example.dor.models.Category;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<Category> categories;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryName;
        private final TextView wordCountText;
        private final View selectionIndicator;
        private final MaterialCardView cardView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            wordCountText = itemView.findViewById(R.id.wordCountText);
            selectionIndicator = itemView.findViewById(R.id.selectionIndicator);
            cardView = (MaterialCardView) itemView;
        }

        public void bind(Category category, int position) {
            // Show name with emoji prefix for visual distinction
            categoryName.setText(category.getEmoji() + "  " + category.getName());

            // Show word count
            int wordCount = category.getWordCount();
            if (wordCount > 0) {
                wordCountText.setVisibility(View.VISIBLE);
                wordCountText.setText(wordCount + " کلمه");
            } else {
                wordCountText.setVisibility(View.GONE);
            }

            // Update selection state
            if (category.isSelected()) {
                cardView.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.primary));
                cardView.setStrokeWidth(4);
                cardView.setCardElevation(6);
                selectionIndicator.setVisibility(View.VISIBLE);
            } else {
                cardView.setStrokeColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                cardView.setStrokeWidth(0);
                cardView.setCardElevation(2);
                selectionIndicator.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category, position);
                }
            });
        }
    }
}
