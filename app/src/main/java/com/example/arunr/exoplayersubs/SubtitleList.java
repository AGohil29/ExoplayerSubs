package com.example.arunr.exoplayersubs;

import android.view.ActionProvider;
import android.widget.ImageView;

/**
 * Created by arun.r on 14-06-2018.
 */

public class SubtitleList {

    private String subtitleLanguage;
    private boolean checked;

    public SubtitleList() {

    }

    public SubtitleList(String subtitleLanguage) {
        this.subtitleLanguage = subtitleLanguage;
    }

    public String getSubtitleLanguage() {
        return subtitleLanguage;
    }

    public void setSubtitleLanguage(String subtitleLanguage) {
        this.subtitleLanguage = subtitleLanguage;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
