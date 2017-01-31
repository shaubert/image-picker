package com.shaubert.ui.imagepicker;

import android.graphics.drawable.Drawable;
import android.view.View;

public interface ImageTarget {

    void setImage(Drawable drawable);

    View getView();

    Drawable getCurrentImage();

}
