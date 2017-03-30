package com.shaubert.ui.imagepicker;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.edmodo.cropper.CropImageView;
import com.shaubert.ui.dialogs.ProgressDialogManager;

import java.io.IOException;
import java.io.OutputStream;

public class CropImageActivity extends FragmentActivity {

    public static final String TAG = CropImageActivity.class.getSimpleName();

    public static final String CROP_OPTIONS_EXTRA = "crop_options_extra";

    private CropImageView cropImageView;
    private CropOptions cropOptions;
    private Handler handler = new Handler();
    private ProgressDialogManager progressDialog;
    private boolean imageSet;

    private static AsyncTask<Void, Void, Boolean> cropTask;
    private static Boolean cropResult;
    private static Runnable cropCallback;

    private Runnable activityCropCallback = new Runnable() {
        @Override
        public void run() {
            if (cropResult == null) {
                return;
            }

            progressDialog.hideDialog();
            if (cropResult) {
                finishWithOk();
            } else {
                new ToastErrorPresenter().showStorageError(CropImageActivity.this);
            }
            cropResult = null;
        }
    };

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
        }

        progressDialog = new ProgressDialogManager(this, "crop-progress");
        progressDialog.setMessage(getText(R.string.sh_image_picker_crop_image_processing));
        progressDialog.setCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelCropTask();
            }
        });

        cropOptions = getCropOptions(getIntent());
        if (cropOptions == null
                || cropOptions.getInUri() == null
                || cropOptions.getOutUri() == null) {
            new ToastErrorPresenter().showLoadingError(CropImageActivity.this);
            return;
        }

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        DoneCancelView doneCancelView = new DoneCancelView(this);
        doneCancelView.setCallback(new DoneCancelView.Callback() {
            @Override
            public boolean onDone() {
                saveResult();
                return false;
            }

            @Override
            public boolean onCancel() {
                finishWithCancel();
                return false;
            }
        });
        content.addView(doneCancelView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.sh_image_picker_done_cancel_height)));

        cropImageView = new CropImageView(this);
        cropImageView.setId(R.id.sh_image_picker_crop_image_view);
        content.addView(cropImageView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(content, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        getWindow().getDecorView().setBackground(new ColorDrawable(Color.BLACK));

        ImagePickerConfig.getImageLoader().loadImage(Uri.decode(cropOptions.getInUri().toString()), new ImageLoader.LoadingCallback<Bitmap>() {
            @Override
            public void onLoadingStarted(String imageUri) {
            }

            @Override
            public void onLoadingComplete(String imageUri, Bitmap loadedImage) {
                cropImageView.setImageBitmap(loadedImage);
                imageSet = true;

                final int aspectX = cropOptions.getAspectX();
                final int aspectY = cropOptions.getAspectY();
                if (aspectX > 0 && aspectY > 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            cropImageView.setFixedAspectRatio(true);
                            cropImageView.setAspectRatio(aspectX, aspectY);
                        }
                    });
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, Exception ex) {
                new ToastErrorPresenter().showLoadingError(CropImageActivity.this);
                finishWithCancel();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cropCallback = activityCropCallback;
        activityCropCallback.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cropCallback = null;
        if (isFinishing()) {
            cropTask = null;
        }
    }

    private void cancelCropTask() {
        if (cropTask != null) {
            cropTask.cancel(true);
            cropTask = null;
        }
        cropResult = null;
    }

    private void finishWithCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void saveResult() {
        if (!imageSet) return;

        if (validateMinSizes()) {
            Bitmap image = cropImageView.getCroppedImage();
            int maxWidth = cropOptions.getMaxWidth();
            int maxHeight = cropOptions.getMaxHeight();
            int targetW = maxWidth > 0 ? maxWidth : image.getWidth();
            int targetH = maxHeight > 0 ? maxHeight : image.getHeight();
            if (image.getHeight() > targetH || image.getWidth() > targetW) {
                float scale = Math.max((float) image.getWidth() / targetW, (float) image.getHeight() / targetH);
                int w = (int) (image.getWidth() / scale);
                int h = (int) (image.getHeight() / scale);
                Bitmap newImage = Bitmap.createScaledBitmap(image, w, h, false);
                if (newImage != image) {
                    image.recycle();
                    image = newImage;
                }
            }
            startCropTask(image);
        }
    }

    private void startCropTask(final Bitmap image) {
        cancelCropTask();

        progressDialog.showDialog();
        cropTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                OutputStream outputStream = null;
                try {
                    outputStream = getContentResolver().openOutputStream(cropOptions.getOutUri());
                    image.compress(cropOptions.getCompressFormat(), cropOptions.getCompressQuality(), outputStream);
                    return true;
                } catch (IOException e) {
                    Log.e(TAG, "failed to save result image", e);
                    return false;
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                image.recycle();
                cropResult = result;
                if (cropCallback != null) {
                    cropCallback.run();
                }
            }

            @Override
            protected void onCancelled() {
                image.recycle();
            }
        };
        cropTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void finishWithOk() {
        Intent data = new Intent();
        data.putExtras(buildCropImageExtras(cropOptions));
        setResult(RESULT_OK, data);
        finish();
    }

    private boolean validateMinSizes() {
        if (!imageSet) return false;

        RectF cropRect = cropImageView.getActualCropRect();
        int minWidth = cropOptions.getMinWidth();
        int minHeight = cropOptions.getMinHeight();
        if ((minWidth > 0 && cropRect.width() < minWidth)
                || (minHeight > 0 && cropRect.height() < minHeight)) {
            if (minWidth > 0 && minHeight > 0) {
                Toast.makeText(this, getString(R.string.sh_image_picker_crop_image_too_small_format, minWidth, minHeight),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.sh_image_picker_crop_image_too_small, Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    public static Bundle buildCropImageExtras(CropOptions cropOptions) {
        final Bundle b = new Bundle();
        b.putParcelable(CROP_OPTIONS_EXTRA, cropOptions);
        return b;
    }

    public static CropOptions getCropOptions(Intent intent) {
        return intent.getParcelableExtra(CROP_OPTIONS_EXTRA);
    }

}
