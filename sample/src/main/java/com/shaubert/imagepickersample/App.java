package com.shaubert.imagepickersample;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.shaubert.ui.imagepicker.ImagePickerConfig;
import com.shaubert.ui.imagepicker.glide.GlideImageLoader;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        setup();
    }

    private void setup() {
        GlideImageLoader imageLoader = new GlideImageLoader(this) {
            protected BitmapRequestBuilder<Uri, Bitmap> configure(BitmapTypeRequest<Uri> request) {
                return request
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .animate(android.R.anim.fade_in);
            }

            @Override
            protected DrawableRequestBuilder<Uri> configure(DrawableTypeRequest<Uri> request) {
                return request
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .animate(android.R.anim.fade_in);
            }
        };
        ImagePickerConfig.setup()
                .imageLoader(imageLoader)
                .apply();
    }

}
