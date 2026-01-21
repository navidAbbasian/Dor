package com.example.dor.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dor.R;
import com.example.dor.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private OnCategoryClickListener listener;

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
        private TextView emojiText;
        private TextView categoryName;
        private ImageView checkIcon;
        private CardView cardView;
        private LinearLayout categoryContainer;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            emojiText = itemView.findViewById(R.id.emojiText);
            categoryName = itemView.findViewById(R.id.categoryName);
            checkIcon = itemView.findViewById(R.id.checkIcon);
            cardView = (CardView) itemView;
            categoryContainer = itemView.findViewById(R.id.categoryContainer);
        }

        public void bind(Category category, int position) {
            emojiText.setText(category.getEmoji());
            categoryName.setText(category.getName());

            if (category.isSelected()) {
                cardView.setCardBackgroundColor(Color.parseColor("#E94560"));
                checkIcon.setVisibility(View.VISIBLE);
            } else {
                cardView.setCardBackgroundColor(Color.parseColor("#0F3460"));
                checkIcon.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category, position);
                }
            });
        }
    }
}
