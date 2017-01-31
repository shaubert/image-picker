package com.shaubert.ui.imagepicker;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import com.shaubert.lifecycle.objects.LifecycleObjectsGroup;
import com.shaubert.ui.imagepicker.nostra.Scheme;

import java.io.File;

public class ImagePicker extends LifecycleObjectsGroup {

    private ImageTarget imageTarget;
    private View takePictureButton;
    private View progressView;
    private View errorView;

    private ImagePickerController controller;
    private String imageUrl;
    private String defaultImageUrl;
    private String currentUrl;
    private CompressionOptions compressionOptions;
    private String tag;

    public ImagePicker(@NonNull Fragment fragment, @NonNull String tag) {
        this.tag = tag;
        controller = new ImagePickerController(fragment, createControllerCallback(), tag);
        attachToLifecycle(controller);
    }

    public ImagePicker(@NonNull FragmentActivity fragmentActivity, @NonNull String tag) {
        this.tag = tag;
        controller = new ImagePickerController(fragmentActivity, createControllerCallback(), tag);
        attachToLifecycle(controller);
    }

    private ImagePickerController.Callback createControllerCallback() {
        return new ImagePickerController.Callback() {
            @Override
            public void onImageFileSet(@Nullable File imageFile) {
                ImagePicker.this.onImageFileSet(imageFile);
            }

            @Override
            public void onStateChanged(ImagePickerController.State state) {
                ImagePicker.this.onStateChanged(state);
            }

            @Override
            public ImageTarget getImageTarget() {
                return ImagePicker.this.getImageTarget();
            }

            @Override
            public CompressionOptions getCompressionOptions(@NonNull File imageFile) {
                return ImagePicker.this.getCompressionOptions();
            }
        };
    }

    public void setupViews(@Nullable ImageTarget imageTarget, @Nullable View takePictureButton, @Nullable View progressView, @Nullable View errorView) {
        if (this.takePictureButton != null) {
            this.takePictureButton.setOnClickListener(null);
        }
        if (this.errorView != null) {
            this.errorView.setOnClickListener(null);
        }

        this.takePictureButton = takePictureButton;
        this.progressView = progressView;
        this.errorView = errorView;

        setImageTarget(imageTarget);
        if (takePictureButton != null) {
            takePictureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleTakePictureButtonClick();
                }
            });
            refreshTakePictureButtonVisibility();
        }
        if (errorView != null) {
            errorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getController().retryLoading();
                }
            });
        }
    }

    public void handleTakePictureButtonClick() {
        if (controller.getState() == ImagePickerController.State.EMPTY) {
            showAddDialog();
        } else {
            if (currentImageIsDefault()) {
                showAddDialog();
            } else {
                showEditDialog();
            }
        }
    }

    public boolean showEditDialog() {
        if (controller.getState() != ImagePickerController.State.EMPTY && !currentImageIsDefault()) {
            controller.showEditDialog();
            return true;
        }
        return false;
    }

    public void showAddDialog() {
        controller.showAddDialog();
    }

    public void takePhoto() {
        getController().onTakePhotoClicked();
    }

    public void pickPicture() {
        getController().onPickPictureClicked();
    }

    public void removeImage() {
        getController().onRemoveImageClicked();
    }

    public void showImageFullScreen() {
        getController().showImageFullScreen();
    }

    private void refreshTakePictureButtonVisibility() {
        if (takePictureButton != null) {
            takePictureButton.setVisibility(isReadonly() ? View.GONE : View.VISIBLE);
        }
    }

    private void onStateChanged(ImagePickerController.State state) {
        switch (state) {
            case EMPTY:
            case WITH_IMAGE:
                if (progressView != null) progressView.setVisibility(View.GONE);
                if (errorView != null) errorView.setVisibility(View.GONE);
                break;
            case LOADING:
            case PROCESSING:
                if (progressView != null) progressView.setVisibility(View.VISIBLE);
                if (errorView != null) errorView.setVisibility(View.GONE);
                break;
            case ERROR:
                if (errorView != null) errorView.setVisibility(View.VISIBLE);
                if (progressView != null) progressView.setVisibility(View.GONE);
                break;
        }
        refreshTakePictureButtonVisibility();
    }

    private void setImageTarget(final ImageTarget imageTarget) {
        this.imageTarget = imageTarget;
        if (imageTarget == null) {
            return;
        }

        if (!TextUtils.isEmpty(defaultImageUrl)) {
            loadImage(defaultImageUrl);
        }
        onStateChanged(controller.getState());
    }

    public View.OnClickListener createImageClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controller.getState() == ImagePickerController.State.WITH_IMAGE) {
                    if (controller.isReadonly()) {
                        imageTarget.getView().getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                controller.showImageFullScreen();
                            }
                        }, 250);
                    } else {
                        showEditDialog();
                    }
                } else if (!controller.isReadonly()
                        && controller.getState() == ImagePickerController.State.EMPTY) {
                    showAddDialog();
                }
            }
        };
    }

    public void setDefaultImageUrl(String imageUrl) {
        defaultImageUrl = imageUrl;
        if (!hasImage()) {
            loadImage(imageUrl);
        }
    }

    private void onImageFileSet(File imageFile) {
        String imageUrl = imageFile != null ? Scheme.FILE.wrap(imageFile.getPath()) : null;
        if (imageUrl == null
                || imageUrl.equals(defaultImageUrl)) {
            this.imageUrl = null;
            loadImage(defaultImageUrl);
        } else {
            this.imageUrl = imageUrl;
            loadImage(imageUrl);
        }
    }

    public void setImageUrl(String imageUrl) {
        if (!TextUtils.equals(this.imageUrl, imageUrl)) {
            controller.clear();
            this.imageUrl = imageUrl;
            if (TextUtils.isEmpty(imageUrl)) {
                return;
            }

            if (Scheme.FILE.belongsTo(imageUrl)) {
                File file = new File(Scheme.FILE.crop(imageUrl));
                if (file.exists()) {
                    setImageFile(file);
                    return;
                }
            }

            ImagePickerConfig.getImageLoader().save(imageUrl, new ImageLoader.SaveCallback() {
                @Override
                public void onLoadingStarted(String imageUri) {

                }

                @Override
                public void onSaved(String imageUri, File file) {
                    if (TextUtils.equals(ImagePicker.this.imageUrl, imageUri)) {
                        setImageFile(file);
                    }
                }

                @Override
                public void onLoadingFailed(String imageUri, Exception ex) {

                }
            });
        }
    }

    public void setImageFile(File imageFile) {
        controller.setImageFile(imageFile);
    }

    private void loadImage(String imageUrl) {
        if (imageTarget != null) {
            if (TextUtils.equals(currentUrl, imageUrl)) {
                controller.onLoadingComplete(imageUrl);
            } else {
                ImagePickerConfig.getImageLoader().loadImage(imageUrl, imageTarget, new ImageLoader.LoadingCallback<Drawable>() {
                    @Override
                    public void onLoadingStarted(String imageUri) {
                        controller.onLoadingStarted(imageUri);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, Drawable loadedImage) {
                        ImagePicker.this.currentUrl = imageUri;
                        controller.onLoadingComplete(imageUri);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, Exception ex) {
                        controller.onLoadingFailed(imageUri);
                    }
                });
            }
        } else {
            controller.onLoadingComplete(imageUrl);
        }
    }

    public void clear() {
        imageUrl = null;
        controller.clear();
        if (defaultImageUrl != null) {
            loadImage(defaultImageUrl);
        }
    }

    @Override
    protected void onPause(boolean isFinishing) {
        super.onPause(isFinishing);
        if (isFinishing) controller.removeTempFiles();
    }

    @Override
    protected String getBundleTag() {
        return super.getBundleTag() + tag;
    }

    protected ImagePickerController getController() {
        return controller;
    }

    public void setPrivatePhotos(boolean privatePhotos) {
        controller.setPrivatePhotos(privatePhotos);
    }

    public boolean isPrivatePhotos() {
        return controller.isPrivatePhotos();
    }

    public void setCropCallback(ImagePickerController.CropCallback cropCallback) {
        controller.setCropCallback(cropCallback);
    }

    public void setCompressionOptions(CompressionOptions compressionOptions) {
        this.compressionOptions = compressionOptions;
    }

    public CompressionOptions getCompressionOptions() {
        return compressionOptions;
    }

    public File getImageFile() {
        return controller.getImageFile();
    }

    public Uri getImageUri() {
        return controller.getImageUri();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean currentImageIsDefault() {
        return !hasImage() && !TextUtils.isEmpty(defaultImageUrl);
    }

    public String getDefaultImageUrl() {
        return defaultImageUrl;
    }

    public boolean hasImage() {
        return imageUrl != null && controller.hasImage();
    }

    public boolean hasUserImage() {
        return controller.hasUserImage();
    }

    public void setReadonly(boolean readonly) {
        controller.setReadonly(readonly);
        refreshTakePictureButtonVisibility();
    }

    public boolean isReadonly() {
        return controller.isReadonly();
    }

    public void setListener(ImagePickerController.ImageListener listener) {
        controller.setImageListener(listener);
    }

    public ImageTarget getImageTarget() {
        return imageTarget;
    }

}