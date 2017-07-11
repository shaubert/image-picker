package com.shaubert.imagepickersample;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import com.bumptech.glide.*;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
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
            @Override
            protected void loadBitmapWithGlide(Uri uri, Target<Bitmap> target) {
                Glide.with(App.this)
                        .load(uri)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .animate(android.R.anim.fade_in)
                        .into(target);
            }

            @Override
            protected void loadDrawableWithGlide(Context context, Uri uri, Target<GlideDrawable> target) {
                Glide.with(context)
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .animate(android.R.anim.fade_in)
                        .into(target);
            }
        };
        ImagePickerConfig.setup()
                .imageLoader(imageLoader)
                .apply();
    }

}
