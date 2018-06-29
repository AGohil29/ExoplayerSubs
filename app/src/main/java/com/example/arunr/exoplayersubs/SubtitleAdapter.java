package com.example.arunr.exoplayersubs;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

/**
 * Created by arun.r on 14-06-2018.
 */

public class SubtitleAdapter extends RecyclerView.Adapter<SubtitleAdapter.MyViewHolder> {

    private List<SubtitleList> subtitles;
    private Context context;
    public int position;
    private onRecyclerViewItemClickListener mItemClickListener;
    private int lastCheckedPosition = -1;

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
    public void onBindViewHolder(@NonNull final SubtitleAdapter.MyViewHolder holder, int position) {
        SubtitleList subtitleList = subtitles.get(position);
        holder.subtitleLang.setText(subtitleList.getSubtitleLanguage());

        // in some cases, it will prevent unwanted situations
        holder.subsSelected.setOnCheckedChangeListener(null);

        // if true, your checkbox will be selected, else unselected
        holder.subsSelected.setChecked(position == lastCheckedPosition);

        // get data from share preference about selected Subtitle
        // if else statement
        // for true make check box is checked true


    }

    @Override
    public int getItemCount() {
        return subtitles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView subtitleLang;
        public RadioButton subsSelected;

        public MyViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            subtitleLang = view.findViewById(R.id.subtitle_language);
            subsSelected = view.findViewById(R.id.subs_checked);
            subsSelected.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClickListener(view, getAdapterPosition());
            }
            int copyOfLastCheckedPosition = lastCheckedPosition;
            lastCheckedPosition = getAdapterPosition();
            notifyItemChanged(copyOfLastCheckedPosition);
            notifyItemChanged(lastCheckedPosition);
            notifyDataSetChanged();
            // checkbox is checked and unchecked
            /*if (subsSelected.isChecked()) {
                subsSelected.setChecked(false);
            } else {
                subsSelected.setChecked(true);
            }*/

            // Todo Create share preference

            // Add data to preference

        }
    }


}
