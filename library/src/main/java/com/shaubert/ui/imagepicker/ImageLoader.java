package com.shaubert.ui.imagepicker;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.File;

public interface ImageLoader {

    void loadImage(String url, LoadingCallback<Bitmap> loadingCallback);

    void loadImage(String url, ImageTarget target, LoadingCallback<Drawable> loadingCallback);

    void save(String url, SaveCallback saveCallback);

    interface LoadingCallback<T> {

        void onLoadingStarted(String imageUri);

        void onLoadingComplete(String imageUri, T loadedImage);

        void onLoadingFailed(String imageUri, Exception ex);

    }

    interface SaveCallback {

        void onLoadingStarted(String imageUri);

        void onSaved(String imageUri, File file);

        void onLoadingFailed(String imageUri, Exception ex);

    }

}
