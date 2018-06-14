package com.example.arunr.exoplayersubs;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by arun.r on 14-06-2018.
 */

public class SubtitleAdapter extends RecyclerView.Adapter<SubtitleAdapter.MyViewHolder> {

    private List<SubtitleList> subtitles;

    public SubtitleAdapter(List<SubtitleList> subtitles) {
        this.subtitles = subtitles;
    }

    @NonNull
    @Override
    public SubtitleAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subtitle_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtitleAdapter.MyViewHolder holder, int position) {
        SubtitleList subtitleList = subtitles.get(position);
        holder.subtitleLang.setText(subtitleList.getSubtitleLanguage());
    }

    @Override
    public int getItemCount() {
        return subtitles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView subtitleLang;

        public MyViewHolder(View view) {
            super(view);
            subtitleLang = view.findViewById(R.id.subtitle_language);
        }
    }

}
