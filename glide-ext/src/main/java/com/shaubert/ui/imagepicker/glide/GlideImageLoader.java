package com.shaubert.ui.imagepicker.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.shaubert.ui.imagepicker.ImageLoader;
import com.shaubert.ui.imagepicker.ImageTarget;

import java.io.File;

public abstract class GlideImageLoader implements ImageLoader {

    private Context context;

    @SuppressWarnings("WeakerAccess")
    public GlideImageLoader(Context appContext) {
        this.context = appContext.getApplicationContext();
    }

    @Override
    public final void loadImage(final Uri uri, final LoadingCallback<Bitmap> loadingCallback) {
        loadBitmapWithGlide(uri, new SimpleTarget<Bitmap>() {
            @Override
            public void onLoadStarted(Drawable placeholder) {
                if (loadingCallback != null) loadingCallback.onLoadingStarted(uri);
                super.onLoadStarted(placeholder);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                if (loadingCallback != null) loadingCallback.onLoadingFailed(uri, e);
                super.onLoadFailed(e, errorDrawable);
            }

            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                if (loadingCallback != null) loadingCallback.onLoadingComplete(uri, resource);
            }
        });
    }

    @SuppressWarnings("WeakerAccess")
    protected void loadBitmapWithGlide(Uri uri, Target<Bitmap> target) {
        Glide.with(context)
                .load(uri)
                .asBitmap()
                .into(target);
    }

    @Override
    public final void loadImage(final Uri uri, final ImageTarget target, final LoadingCallback<Drawable> loadingCallback) {
        loadDrawableWithGlide(target.getView().getContext(), uri,
                new ViewTarget<View, GlideDrawable>(target.getView()) {
                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        if (loadingCallback != null) loadingCallback.onLoadingStarted(uri);
                        super.onLoadStarted(placeholder);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        if (loadingCallback != null) loadingCallback.onLoadingFailed(uri, e);
                        super.onLoadFailed(e, errorDrawable);
                    }

                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        if (loadingCallback != null) loadingCallback.onLoadingComplete(uri, resource);

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

    @SuppressWarnings("WeakerAccess")
    protected void loadDrawableWithGlide(Context context, Uri uri, Target<GlideDrawable> target) {
        Glide.with(context)
                .load(uri)
                .into(target);
    }

    @Override
    public final void save(final Uri uri, final SaveCallback saveCallback) {
        saveWithGlide(uri, new SimpleTarget<File>() {
            @Override
            public void onLoadStarted(Drawable placeholder) {
                if (saveCallback != null) saveCallback.onLoadingStarted(uri);
                super.onLoadStarted(placeholder);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                if (saveCallback != null) saveCallback.onLoadingFailed(uri, e);
                super.onLoadFailed(e, errorDrawable);
            }

            @Override
            public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                if (saveCallback != null) saveCallback.onSaved(uri, resource);
            }
        });
    }

    @SuppressWarnings("WeakerAccess")
    protected void saveWithGlide(Uri uri, Target<File> target) {
        Glide.with(context)
                .load(uri)
                .downloadOnly(target);
    }

}
