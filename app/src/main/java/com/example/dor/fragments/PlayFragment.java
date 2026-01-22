package com.example.dor.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.dor.R;
import com.example.dor.SetupActivity;

/**
 * Fragment for starting the game flow - player count selection
 */
public class PlayFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup card click listeners
        view.findViewById(R.id.card4Players).setOnClickListener(v -> startSetup(4));
        view.findViewById(R.id.card6Players).setOnClickListener(v -> startSetup(6));
        view.findViewById(R.id.card8Players).setOnClickListener(v -> startSetup(8));
        view.findViewById(R.id.card10Players).setOnClickListener(v -> startSetup(10));

        // Add ripple effect animation
        addCardAnimation(view.findViewById(R.id.card4Players));
        addCardAnimation(view.findViewById(R.id.card6Players));
        addCardAnimation(view.findViewById(R.id.card8Players));
        addCardAnimation(view.findViewById(R.id.card10Players));
    }

    private void addCardAnimation(CardView card) {
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }

    private void startSetup(int playerCount) {
        Intent intent = new Intent(requireContext(), SetupActivity.class);
        intent.putExtra("playerCount", playerCount);
        startActivity(intent);
    }
}

