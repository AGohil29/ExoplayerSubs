package com.example.arunr.exoplayersubs;

import android.view.ActionProvider;
import android.widget.ImageView;

/**
 * Created by arun.r on 14-06-2018.
 */

public class SubtitleList {

    private String subtitleLanguage;
    private boolean isCheckedImage;

    public SubtitleList() {

    }

    public SubtitleList(String subtitleLanguage, boolean isCheckedImage) {
        this.subtitleLanguage = subtitleLanguage;
        this.isCheckedImage = isCheckedImage;
    }

    public String getSubtitleLanguage() {
        return subtitleLanguage;
    }

    public void setSubtitleLanguage(String subtitleLanguage) {
        this.subtitleLanguage = subtitleLanguage;
    }

    public boolean isCheckedImage() {
        return isCheckedImage;
    }

    public void setCheckedImage(boolean checkedImage) {
        isCheckedImage = checkedImage;
    }
}
