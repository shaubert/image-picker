package com.shaubert.ui.imagepicker;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.shaubert.lifecycle.objects.LifecycleObjectsGroup;

@SuppressWarnings("WeakerAccess")
public class ImagePicker extends LifecycleObjectsGroup {

    private ImageTarget imageTarget;
    private View takePictureButton;
    private View progressView;
    private View errorView;

    private ImagePickerController controller;
    private Uri imageUri;
    private Uri defaultImageUri;
    private Drawable defaultImageDrawable;
    private Uri currentUri;
    private String tag;

    private boolean stopped = true;

    public ImagePicker(ImagePickerController controller, @NonNull String tag) {
        this.tag = tag;
        this.controller = controller;
        controller.setCallback(createControllerCallback());
        if (!controller.isAttached()) {
            attachToLifecycle(controller);
        }
    }

    private ImagePickerController.Callback createControllerCallback() {
        return new ImagePickerController.Callback() {
            @Override
            public void onImageUriSet(@Nullable Uri imageUri, boolean userPickedImage) {
                ImagePicker.this.onImageFileSet(imageUri, userPickedImage);
            }

            @Override
            public void onStateChanged(ImagePickerController.State state) {
                ImagePicker.this.onStateChanged(state);
            }

            @Override
            public ImageTarget getImageTarget() {
                return ImagePicker.this.getImageTarget();
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

    @SuppressWarnings("unused")
    public void takePhoto() {
        getController().onTakePhotoClicked();
    }

    @SuppressWarnings("unused")
    public void pickPicture() {
        getController().onPickPictureClicked();
    }

    @SuppressWarnings("unused")
    public void removeImage() {
        getController().onRemoveImageClicked();
    }

    @SuppressWarnings("unused")
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
        currentUri = null;
        if (imageTarget == null) {
            return;
        }

        onImageFileSet(getImage(), hasUserImage());
        onStateChanged(controller.getState());
    }

    private void setDefaultImage() {
        if (imageTarget == null) {
            currentUri = null;
            return;
        }

        if (defaultImageDrawable != null) {
            currentUri = null;
            imageTarget.setImage(defaultImageDrawable);
        } else {
            loadImage(defaultImageUri);
        }
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

    public void setDefaultImageDrawable(Drawable defaultImageDrawable) {
        this.defaultImageDrawable = defaultImageDrawable;
        if (!hasImage() && imageTarget != null) {
            imageTarget.setImage(defaultImageDrawable);
        }
    }

    @SuppressWarnings("unused")
    public void setDefaultImageUri(Uri imageUri) {
        defaultImageUri = imageUri;
        if (!hasImage()) {
            loadImage(imageUri);
        }
    }

    private void onImageFileSet(Uri imageUri, boolean userPickedImage) {
        if (imageUri == null
                || imageUri.equals(defaultImageUri)) {
            this.imageUri = null;
            setDefaultImage();
        } else {
            if (userPickedImage) {
                this.imageUri = imageUri;
            }
            loadImage(imageUri);
        }
    }

    public void setImage(Uri imageUri) {
        if (!Objects.equals(this.imageUri, imageUri)) {
            controller.clear();
            this.imageUri = imageUri;
            if (imageUri == null) {
                return;
            }

            controller.setImage(imageUri);
        }
    }

    private void loadImage(Uri imageUri) {
        if (imageTarget != null) {
            if (imageUri == null) {
                currentUri = null;
                imageTarget.setImage(null);
            } else if (Objects.equals(currentUri, imageUri)) {
                controller.onLoadingComplete(imageUri);
            } else {
                ImagePickerConfig.getImageLoader().loadImage(imageUri, imageTarget, new ImageLoader.LoadingCallback<Drawable>() {
                    @Override
                    public void onLoadingStarted(Uri imageUri) {
                        if (stopped) return;

                        controller.onLoadingStarted(imageUri);
                    }

                    @Override
                    public void onLoadingComplete(Uri imageUri, Drawable loadedImage) {
                        if (stopped) return;

                        ImagePicker.this.currentUri = imageUri;
                        controller.onLoadingComplete(imageUri);
                    }

                    @Override
                    public void onLoadingFailed(Uri imageUri, Exception ex) {
                        if (stopped) return;

                        controller.onLoadingFailed(imageUri);

                        if (imageUri != defaultImageUri) {
                            setDefaultImage();
                        } else {
                            currentUri = null;
                            imageTarget.setImage(null);
                        }
                    }
                });
            }
        } else {
            controller.onLoadingComplete(imageUri);
        }
    }

    @SuppressWarnings("unused")
    public void clear() {
        imageUri = null;
        currentUri = null;
        controller.clear();
        setDefaultImage();
    }

    @Override
    protected void onPause(boolean isFinishing) {
        super.onPause(isFinishing);
        if (isFinishing) controller.removeTempFiles();
    }

    @Override
    protected void onStop(boolean isFinishing) {
        stopped = true;
    }

    @Override
    protected void onStart() {
        stopped = false;

        if (imageUri == null) {
            setDefaultImage();
        } else if (!Objects.equals(currentUri, imageUri)) {
            loadImage(imageUri);
        }
    }

    @Override
    protected String getBundleTag() {
        return super.getBundleTag() + tag;
    }

    protected ImagePickerController getController() {
        return controller;
    }

    @SuppressWarnings("unused")
    public boolean isPrivatePhotos() {
        return controller.isPrivatePhotos();
    }

    public void setCropCallback(ImagePickerController.CropCallback cropCallback) {
        controller.setCropCallback(cropCallback);
    }

    public void setCompressionCallback(ImagePickerController.CompressionCallback compressionCallback) {
        controller.setCompressionCallback(compressionCallback);
    }

    public Uri getImage() {
        return controller.getImage();
    }

    public boolean currentImageIsDefault() {
        return !hasImage() && (defaultImageUri != null || defaultImageDrawable != null);
    }

    @SuppressWarnings("unused")
    public Uri getDefaultImageUri() {
        return defaultImageUri;
    }

    @SuppressWarnings("unused")
    public Drawable getDefaultImageDrawable() {
        return defaultImageDrawable;
    }

    public boolean hasImage() {
        return imageUri != null && controller.hasImage();
    }

    public boolean hasUserImage() {
        return controller.hasUserImage();
    }

    @SuppressWarnings("unused")
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