package com.example.clock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LapAdapter extends RecyclerView.Adapter<LapAdapter.LapViewHolder> {
    private List<String> lapList;
    private Context context;

    public LapAdapter(Context context, List<String> lapList) {
        this.context = context;
        this.lapList = lapList;
    }

    @NonNull
    @Override
    public LapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.lap_item, parent, false);
        return new LapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LapViewHolder holder, int position) {
        String lapText = lapList.get(position);
        holder.lapTextView.setText(lapText);
        holder.lapTextView.setTextColor(ContextCompat.getColor(context, R.color.LightPurple));
    }


    @Override
    public int getItemCount() {
        return lapList.size();
    }

    public static class LapViewHolder extends RecyclerView.ViewHolder {
        TextView lapTextView;

        public LapViewHolder(View itemView) {
            super(itemView);
            lapTextView = itemView.findViewById(R.id.lapText);
        }
    }
}


