package com.shaubert.ui.imagepicker.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import com.bumptech.glide.*;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.ViewTarget;
import com.shaubert.ui.imagepicker.ImageLoader;
import com.shaubert.ui.imagepicker.ImageTarget;

import java.io.File;

public abstract class GlideImageLoader implements ImageLoader {

    private Context context;

    public GlideImageLoader(Context appContext) {
        this.context = appContext.getApplicationContext();
    }

    @Override
    public void loadImage(final String url, final LoadingCallback<Bitmap> loadingCallback) {
        BitmapTypeRequest<Uri> request = Glide.with(context)
                .load(Uri.parse(url))
                .asBitmap();

        configure(request).into(new SimpleTarget<Bitmap>() {
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

    protected abstract BitmapRequestBuilder<Uri, Bitmap> configure(BitmapTypeRequest<Uri> request);

    @Override
    public void loadImage(final String url, final ImageTarget target, final LoadingCallback<Drawable> loadingCallback) {
        DrawableTypeRequest<Uri> request = Glide.with(target.getView().getContext())
                .load(Uri.parse(url));

        configure(request)
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

    protected abstract DrawableRequestBuilder<Uri> configure(DrawableTypeRequest<Uri> request);

    @Override
    public void save(final String url, final SaveCallback saveCallback) {
        Glide.with(context)
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

}
