package com.example.arunr.exoplayersubs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    // so that the first item in the recycler view is selected
    public int row_index = 0;
    private onRecyclerViewItemClickListener mItemClickListener;
    private int lastCheckedPosition = -1;

    public static final String SHARED_PREF_NAME = "my_pref";
    public static final String SELECTED_SUBTITLE = "subtitleName";
    public static final String SELECTED_MEDIASOURCE = "selectedMediasource";

    public SubtitleAdapter() {

    }


    public SubtitleAdapter(List<SubtitleList> subtitles, Context context) {
        this.subtitles = subtitles;
        this.context = context;
    }

    public void setOnItemClickListener(onRecyclerViewItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface onRecyclerViewItemClickListener {
        void onItemClickListener(RecyclerView.ViewHolder holder, int position);
    }

    @NonNull
    @Override
    public SubtitleAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subtitle_list_row, parent, false);
        //Store value here
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        row_index = sharedPreferences.getInt(SELECTED_SUBTITLE, 0);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final SubtitleAdapter.MyViewHolder holder, int position) {
        SubtitleList subtitleList = subtitles.get(position);
        holder.subtitleLang.setText(subtitleList.getSubtitleLanguage());

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                row_index = position; // set row index to selected position
                Common.currentItem = subtitles.get(position); // set current item is item selected
            }
        });

        // Store value in sharedpreference
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        editor = sharedPreferences.edit();
        editor.putInt(SELECTED_SUBTITLE, row_index).apply();

        // highlight color of selected view and display the image
        if (row_index == position) {
            //color of the selected item
            holder.itemView.setBackgroundColor(Color.parseColor("#848282"));
            //show the image when item is clicked
            holder.imageView.setVisibility(View.VISIBLE);
        } else {
            //color when item is not selected
            holder.itemView.setBackgroundColor(Color.parseColor("#696969"));
            //hide the image when another item is clicked
            holder.imageView.setVisibility(View.GONE);
        }

        if (mItemClickListener != null) {
            mItemClickListener.onItemClickListener(holder, row_index);
        }

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
        public ImageView imageView;

        ItemClickListener itemClickListener;

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        public MyViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            subtitleLang = view.findViewById(R.id.subtitle_language);
            imageView = view.findViewById(R.id.subs_checked_image);

        }

        @Override
        public void onClick(View view) {

            itemClickListener.onClick(view, getAdapterPosition());

            int copyOfLastCheckedPosition = lastCheckedPosition;
            lastCheckedPosition = getAdapterPosition();
            notifyItemChanged(copyOfLastCheckedPosition);
            notifyItemChanged(lastCheckedPosition);
            notifyDataSetChanged();

        }
    }


}
