package com.shaubert.ui.imagepicker;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.Nullable;

public interface ImageLoader {

    void loadImage(Uri uri, LoadingCallback<Bitmap> loadingCallback);

    void loadImage(Uri uri, ImageTarget target, LoadingCallback<Drawable> loadingCallback);

    interface LoadingCallback<T> {

        void onLoadingStarted(Uri uri);

        void onLoadingComplete(Uri uri, T loadedImage);

        void onLoadingFailed(Uri uri, @Nullable Exception ex);

        void onLoadingCancelled(Uri uri);

    }

}
