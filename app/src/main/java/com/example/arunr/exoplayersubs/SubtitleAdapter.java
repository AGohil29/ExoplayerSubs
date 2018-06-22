package com.example.arunr.exoplayersubs;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by arun.r on 14-06-2018.
 */

public class SubtitleAdapter extends RecyclerView.Adapter<SubtitleAdapter.MyViewHolder> {

    private List<SubtitleList> subtitles;
    private Context context;
    public int position;
    private onRecyclerViewItemClickListener mItemClickListener;

    public SubtitleAdapter() {

    }

    public SubtitleAdapter(List<SubtitleList> subtitles) {
        this.subtitles = subtitles;
    }

    public void setOnItemClickListener(onRecyclerViewItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface onRecyclerViewItemClickListener {
        void onItemClickListener(View view, int position);
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

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView subtitleLang;
        public CheckBox subsSelected;
        public int subtitleLanguageSelected;

        public MyViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            subtitleLang = view.findViewById(R.id.subtitle_language);
            subsSelected = view.findViewById(R.id.subs_checked);
            subtitleLanguageSelected = getLayoutPosition();

        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClickListener(view, getAdapterPosition());
            }

            // go through each item
            /*position = getAdapterPosition();
            subtitleSelected.selectedSubtitle(subtitles.get(getAdapterPosition()), getAdapterPosition());
            switch (getLayoutPosition()) {
                case 0:
                    // no subs
                    subsSelected.setVisibility(View.VISIBLE);
                    Toast.makeText(view.getContext(), "No subtitles", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    // English subs
                    subsSelected.setVisibility(View.VISIBLE);
                    Toast.makeText(view.getContext(), "English subtitles", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    // Hindi Subs
                    subsSelected.setVisibility(View.VISIBLE);
                    Toast.makeText(view.getContext(), "Hindi subtitles", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    // Marathi Subs
                    subsSelected.setVisibility(View.VISIBLE);
                    Toast.makeText(view.getContext(), "Marathi subtitles", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    // NO subs
            }*/
        }
    }

    public void setSubsPosition(int position) {
        this.position = position;
    }

    public int getSubsPosition(int position) {
        return position;
    }

}
