package com.shaubert.ui.imagepicker;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import com.shaubert.lifecycle.objects.LifecycleObjectsGroup;

import java.io.File;

public class ImagePicker extends LifecycleObjectsGroup {

    private ImageTarget imageTarget;
    private View takePictureButton;
    private View progressView;
    private View errorView;

    private ImagePickerController controller;
    private String imageUrl;
    private String defaultImageUrl;
    private Drawable defaultImageDrawable;
    private String currentUrl;
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
            public void onImageFileSet(@Nullable File imageFile, boolean userPickedImage) {
                ImagePicker.this.onImageFileSet(imageFile, userPickedImage);
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
        currentUrl = null;
        if (imageTarget == null) {
            return;
        }

        onImageFileSet(getImageFile(), hasUserImage());
        onStateChanged(controller.getState());
    }

    private void setDefaultImage() {
        if (imageTarget == null) return;

        if (defaultImageDrawable != null) {
            imageTarget.setImage(defaultImageDrawable);
        } else {
            loadImage(defaultImageUrl);
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

    public void setDefaultImageUrl(String imageUrl) {
        defaultImageUrl = imageUrl;
        if (!hasImage()) {
            loadImage(imageUrl);
        }
    }

    private void onImageFileSet(File imageFile, boolean userPickedImage) {
        String imageUrl = imageFile != null ? Scheme.FILE.wrap(imageFile.getPath()) : null;
        if (imageUrl == null
                || imageUrl.equals(defaultImageUrl)) {
            this.imageUrl = null;
            setDefaultImage();
        } else {
            if (userPickedImage) {
                this.imageUrl = imageUrl;
            }
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
                    if (stopped) return;

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
            } else if (TextUtils.isEmpty(imageUrl)) {
                imageTarget.setImage(null);
            } else {
                ImagePickerConfig.getImageLoader().loadImage(imageUrl, imageTarget, new ImageLoader.LoadingCallback<Drawable>() {
                    @Override
                    public void onLoadingStarted(String imageUri) {
                        if (stopped) return;

                        controller.onLoadingStarted(imageUri);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, Drawable loadedImage) {
                        if (stopped) return;

                        ImagePicker.this.currentUrl = imageUri;
                        controller.onLoadingComplete(imageUri);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, Exception ex) {
                        if (stopped) return;

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
        currentUrl = null;
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
    }

    @Override
    protected String getBundleTag() {
        return super.getBundleTag() + tag;
    }

    protected ImagePickerController getController() {
        return controller;
    }

    public boolean isPrivatePhotos() {
        return controller.isPrivatePhotos();
    }

    public void setCropCallback(ImagePickerController.CropCallback cropCallback) {
        controller.setCropCallback(cropCallback);
    }

    public void setCompressionCallback(ImagePickerController.CompressionCallback compressionCallback) {
        controller.setCompressionCallback(compressionCallback);
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
        return !hasImage() && (!TextUtils.isEmpty(defaultImageUrl) || defaultImageDrawable != null);
    }

    public String getDefaultImageUrl() {
        return defaultImageUrl;
    }

    public Drawable getDefaultImageDrawable() {
        return defaultImageDrawable;
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