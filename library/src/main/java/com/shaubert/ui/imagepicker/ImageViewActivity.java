package com.shaubert.ui.imagepicker;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import java.util.List;

import uk.co.senab.photoview.DefaultOnDoubleTapListener;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageViewActivity extends Activity {

    public static final String IMAGE_URI_EXTRA = "_image_uri_extra";
    public static final String TRANSITION_NAME_EXTRA = "_transition_name_extra";
    public static final ImageView.ScaleType SCALE_TYPE = ImageView.ScaleType.FIT_CENTER;

    private ImageView photoView;

    private boolean animating = true;
    private boolean loading = true;
    private Handler handler = new Handler();
    private PhotoViewAttacher photoViewAttacher;

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri imageUri = getIntent().getParcelableExtra(IMAGE_URI_EXTRA);
        String transitionName = getIntent().getStringExtra(TRANSITION_NAME_EXTRA);

        photoView = new ImageView(this);
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
                connectPhotoAttacher();
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

            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                    disconnectPhotoAttacher();
                    animating = true;
                    super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
                }

                @Override
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                    super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                    animating = false;
                    connectPhotoAttacher();
                }
            });
        } else {
            animating = false;
            connectPhotoAttacher();
        }
    }

    private void connectPhotoAttacher() {
        if (isFinishing() || photoViewAttacher != null) {
            return;
        }

        if (animating || loading) {
            scheduleConnectPhotoAttacher();
            return;
        }

        photoViewAttacher = new PhotoViewAttacher(photoView);
        photoViewAttacher.setScaleType(SCALE_TYPE);
        photoViewAttacher.setMinimumScale(0.5f);
        photoViewAttacher.setOnDoubleTapListener(new DefaultOnDoubleTapListener(photoViewAttacher) {
            @Override
            public boolean onDoubleTap(MotionEvent ev) {
                if (photoViewAttacher == null) {
                    return false;
                } else {
                    try {
                        float e = photoViewAttacher.getScale();
                        float x = ev.getX();
                        float y = ev.getY();
                        if (e < 1) {
                            photoViewAttacher.setScale(1, x, y, true);
                        } else if (e < photoViewAttacher.getMediumScale()) {
                            photoViewAttacher.setScale(photoViewAttacher.getMediumScale(), x, y, true);
                        } else if (e >= photoViewAttacher.getMediumScale() && e < photoViewAttacher.getMaximumScale()) {
                            photoViewAttacher.setScale(photoViewAttacher.getMaximumScale(), x, y, true);
                        } else {
                            photoViewAttacher.setScale(1, x, y, true);
                        }
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                    }
                    return true;
                }
            }
        });
    }

    private void disconnectPhotoAttacher() {
        if (photoViewAttacher != null) {
            photoViewAttacher.cleanup();
            photoViewAttacher = null;
        }
    }

    private void scheduleConnectPhotoAttacher() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectPhotoAttacher();
            }
        }, 500);
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