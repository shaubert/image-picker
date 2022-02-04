package com.shaubert.ui.imagepicker;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class ImageViewTarget implements ImageTarget {

    private ImageView imageView;

    public ImageViewTarget(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    public void setImage(@Nullable Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    @Override
    public View getView() {
        return imageView;
    }

    @Override
    public @Nullable Drawable getCurrentImage() {
        return imageView.getDrawable();
    }

}
