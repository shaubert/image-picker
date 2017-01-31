package com.shaubert.remoteimageviewsample;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.ViewTarget;
import com.shaubert.ui.imagepicker.ImageLoader;
import com.shaubert.ui.imagepicker.ImagePickerConfig;
import com.shaubert.ui.imagepicker.ImageTarget;

import java.io.File;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        setup();
    }

    private void setup() {
        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(final String url, final LoadingCallback<Bitmap> loadingCallback) {
                Glide.with(App.this)
                        .load(Uri.parse(url))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onLoadStarted(Drawable placeholder) {
                                if (loadingCallback != null) loadingCallback.onLoadingStarted(url);
                                super.onLoadStarted(placeholder);
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                if (loadingCallback != null) loadingCallback.onLoadingFailed(url, e);
                                super.onLoadFailed(e, errorDrawable);
                            }

                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                if (loadingCallback != null) loadingCallback.onLoadingComplete(url, resource);
                            }
                        });
            }

            @Override
            public void loadImage(final String url, final ImageTarget target, final LoadingCallback<Drawable> loadingCallback) {
                Glide.with(target.getView().getContext())
                        .load(Uri.parse(url))
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(new ViewTarget<View, GlideDrawable>(target.getView()) {
                            @Override
                            public void onLoadStarted(Drawable placeholder) {
                                if (loadingCallback != null) loadingCallback.onLoadingStarted(url);
                                super.onLoadStarted(placeholder);
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                if (loadingCallback != null) loadingCallback.onLoadingFailed(url, e);
                                super.onLoadFailed(e, errorDrawable);
                            }

                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                if (loadingCallback != null) loadingCallback.onLoadingComplete(url, resource);

                                if (glideAnimation == null || !glideAnimation.animate(resource, new GlideAnimation.ViewAdapter() {
                                    @Override
                                    public View getView() {
                                        return target.getView();
                                    }

                                    @Override
                                    public Drawable getCurrentDrawable() {
                                        return target.getCurrentImage();
                                    }

                                    @Override
                                    public void setDrawable(Drawable drawable) {
                                        target.setImage(drawable);
                                    }
                                })) {
                                    target.setImage(resource);
                                }
                            }
                        });
            }

            @Override
            public void save(final String url, final SaveCallback saveCallback) {
                Glide.with(App.this)
                        .load(Uri.parse(url))
                        .downloadOnly(new SimpleTarget<File>() {
                            @Override
                            public void onLoadStarted(Drawable placeholder) {
                                if (saveCallback != null) saveCallback.onLoadingStarted(url);
                                super.onLoadStarted(placeholder);
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                if (saveCallback != null) saveCallback.onLoadingFailed(url, e);
                                super.onLoadFailed(e, errorDrawable);
                            }

                            @Override
                            public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                                if (saveCallback != null) saveCallback.onSaved(url, resource);
                            }
                        });
            }


        };

        ImagePickerConfig.setup()
                .imageLoader(imageLoader)
                .apply();

    }

}
