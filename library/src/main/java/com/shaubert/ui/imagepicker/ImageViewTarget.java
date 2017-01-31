package com.shaubert.ui.imagepicker;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class ImageViewTarget implements ImageTarget {

    private ImageView imageView;

    public ImageViewTarget(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    public void setImage(Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    @Override
    public View getView() {
        return imageView;
    }

    @Override
    public Drawable getCurrentImage() {
        return imageView.getDrawable();
    }

}
