package com.shaubert.ui.imagepicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.github.chrisbanes.photoview.PhotoView;

public class ImageViewActivity extends Activity {

    public static final String IMAGE_URI_EXTRA = "_image_uri_extra";
    public static final String TRANSITION_NAME_EXTRA = "_transition_name_extra";
    public static final ImageView.ScaleType SCALE_TYPE = ImageView.ScaleType.FIT_CENTER;

    private ImageView photoView;
    private boolean loading = true;

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri imageUri = getIntent().getParcelableExtra(IMAGE_URI_EXTRA);
        String transitionName = getIntent().getStringExtra(TRANSITION_NAME_EXTRA);

        photoView = new PhotoView(this);
        photoView.setScaleType(SCALE_TYPE);
        setContentView(photoView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        getWindow().getDecorView().setBackground(new ColorDrawable(Color.BLACK));

        ActivityCompat.postponeEnterTransition(this);
        ImagePickerConfig.getImageLoader().loadImage(imageUri, new ImageViewTarget(photoView), new ImageLoader.LoadingCallback<Drawable>() {
            @Override
            public void onLoadingStarted(Uri imageUri) {

            }

            @Override
            public void onLoadingComplete(Uri imageUri, Drawable loadedImage) {
                loading = false;
                ActivityCompat.startPostponedEnterTransition(ImageViewActivity.this);
            }

            @Override
            public void onLoadingFailed(Uri imageUri, Exception ex) {
                loading = false;
                new ToastErrorPresenter().showLoadingError(ImageViewActivity.this);
                finish();
            }

            @Override
            public void onLoadingCancelled(Uri uri) {
                if (loading) {
                    loading = false;
                    new ToastErrorPresenter().showLoadingError(ImageViewActivity.this);
                    finish();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
            photoView.setTransitionName(transitionName);
        }
    }

    @SuppressLint("NewApi")
    public static void start(Activity activity,
                             View sharedImageView,
                             Uri imageUri) {
        Intent intent = new Intent(activity, ImagePickerConfig.getOpenImageActivityClass());
        intent.putExtra(ImageViewActivity.IMAGE_URI_EXTRA, imageUri);

        Bundle activityOptions = null;
        if (sharedImageView != null) {
            String transitionName = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    ? sharedImageView.getTransitionName()
                    : null;
            if (!TextUtils.isEmpty(transitionName)) {
                intent.putExtra(ImageViewActivity.TRANSITION_NAME_EXTRA, transitionName);
                activityOptions = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(activity, sharedImageView, transitionName)
                        .toBundle();
            }
        }

        ActivityCompat.startActivity(activity, intent, activityOptions);
    }

}