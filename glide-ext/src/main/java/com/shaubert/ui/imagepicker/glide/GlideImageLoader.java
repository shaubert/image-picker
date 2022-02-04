package com.shaubert.ui.imagepicker.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.shaubert.ui.imagepicker.ImageLoader;
import com.shaubert.ui.imagepicker.ImageTarget;

public abstract class GlideImageLoader implements ImageLoader {

    private final Context context;

    @SuppressWarnings("WeakerAccess")
    public GlideImageLoader(Context appContext) {
        this.context = appContext.getApplicationContext();
    }

    @Override
    public final void loadImage(final Uri uri, final LoadingCallback<Bitmap> loadingCallback) {
        loadBitmapWithGlide(uri, new CustomTarget<Bitmap>() {
            @Override
            public void onLoadStarted(Drawable placeholder) {
                if (loadingCallback != null) loadingCallback.onLoadingStarted(uri);
            }

            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (loadingCallback != null) loadingCallback.onLoadingComplete(uri, resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                if (loadingCallback != null) loadingCallback.onLoadingCancelled(uri);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                if (loadingCallback != null) loadingCallback.onLoadingFailed(uri, null);
            }
        });
    }

    @SuppressWarnings("WeakerAccess")
    protected void loadBitmapWithGlide(Uri uri, Target<Bitmap> target) {
        Glide.with(context)
                .asBitmap()
                .load(uri)
                .into(target);
    }

    @Override
    public final void loadImage(final Uri uri, final ImageTarget target, final LoadingCallback<Drawable> loadingCallback) {
        loadDrawableWithGlide(target.getView().getContext(), uri,
                new CustomViewTarget<View, Drawable>(target.getView()) {
                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {
                        target.setImage(placeholder);

                        if (loadingCallback != null) loadingCallback.onLoadingCancelled(uri);
                    }

                    @Override
                    protected void onResourceLoading(@Nullable Drawable placeholder) {
                        if (placeholder != null) {
                            target.setImage(placeholder);
                        }

                        if (loadingCallback != null) loadingCallback.onLoadingStarted(uri);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        if (errorDrawable != null) {
                            target.setImage(errorDrawable);
                        }

                        if (loadingCallback != null) loadingCallback.onLoadingFailed(uri, null);
                    }

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        if (transition == null || !transition.transition(resource, new Transition.ViewAdapter() {
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

                        if (loadingCallback != null) loadingCallback.onLoadingComplete(uri, resource);
                    }
                });

    }

    @SuppressWarnings("WeakerAccess")
    protected void loadDrawableWithGlide(Context context, Uri uri, Target<Drawable> target) {
        Glide.with(context)
                .load(uri)
                .into(target);
    }

}
