package com.shaubert.ui.imagepicker;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.File;

public interface ImageLoader {

    void loadImage(Uri uri, LoadingCallback<Bitmap> loadingCallback);

    void loadImage(Uri uri, ImageTarget target, LoadingCallback<Drawable> loadingCallback);

    void save(Uri uri, SaveCallback saveCallback);

    interface LoadingCallback<T> {

        void onLoadingStarted(Uri uri);

        void onLoadingComplete(Uri uri, T loadedImage);

        void onLoadingFailed(Uri uri, Exception ex);

    }

    interface SaveCallback {

        void onLoadingStarted(Uri uri);

        void onSaved(Uri uri, File file);

        void onLoadingFailed(Uri uri, Exception ex);

    }

}
