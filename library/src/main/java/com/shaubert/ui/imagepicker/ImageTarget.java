package com.shaubert.ui.imagepicker;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.Nullable;

public interface ImageTarget {

    void setImage(@Nullable Drawable drawable);

    View getView();

    @Nullable Drawable getCurrentImage();

}
