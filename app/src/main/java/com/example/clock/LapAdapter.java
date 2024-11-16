package com.example.clock;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LapAdapter extends RecyclerView.Adapter<LapAdapter.LapViewHolder> {

    private List<String> lapList;

    // Constructor for the adapter
    public LapAdapter(List<String> lapList) {
        this.lapList = lapList;
    }

    @NonNull
    @Override
    public LapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new LapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LapViewHolder holder, int position) {
        String lapTime = lapList.get(position);
        holder.lapTextView.setText(lapTime);
    }

    @Override
    public int getItemCount() {
        return lapList.size();
    }

    public static class LapViewHolder extends RecyclerView.ViewHolder {
        TextView lapTextView;

        public LapViewHolder(View itemView) {
            super(itemView);
            lapTextView = itemView.findViewById(android.R.id.text1); // Default text view ID
        }
    }
}

