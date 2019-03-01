package com.shaubert.ui.imagepicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.shaubert.lifecycle.objects.LifecycleObjectsGroup;
import com.shaubert.m.permission.MultiplePermissionsCallback;
import com.shaubert.m.permission.PermissionsRequest;
import com.shaubert.m.permission.SinglePermissionCallback;

import java.io.File;
import java.util.Collection;

@SuppressWarnings("WeakerAccess")
public class ImagePickerController extends LifecycleObjectsGroup {

    private static final String TEMP_IMAGE_OUTPUT_URI = "__sh_image_picker_temp_image_output_uri";
    private static final String CURRENT_IMAGE_URI = "__sh_image_picker_current_image_uri_extra";
    private static final String STATE = "__sh_image_picker_state_extra";
    private static final String WAITING_FOR_ACTIVITY_RESULT = "__sh_image_picker_waiting_for_activity_result";
    private static final String USER_PICKED_IMAGE = "__sh_image_picker_user_picked_image";

    //Bound to AndroidManifest.xml
    private static final String AUTHORITY_POSTFIX = ".imagepicker.fileprovider";

    private final int REQUEST_CROP = 6709;
    private final int REQUEST_PICK = 9162;
    private final int REQUEST_TAKE_PHOTO = 9163;

    public static final int DEFAULT_MAX_FILE_SIZE = 300 * 1024;
    public static final int DEFAULT_TARGET_IMAGE_WIDTH = 1024;
    public static final int DEFAULT_TARGET_IMAGE_HEIGHT = 1024;

    private Activity activity;
    private Fragment fragment;
    private ErrorPresenter errorPresenter;
    private Compression compression;
    private EditActionsPresenter editActionsPresenter;

    private boolean userPickedImage;
    private Uri imageUri;
    private Uri tempImageOutput;

    private ImageListener listener;
    private Callback callback;
    private String publicDirectoryName;
    private String tag;
    private CropCallback cropCallback;
    private CompressionCallback compressionCallback;
    private boolean privatePhotos;

    private State state;
    private boolean readonly;

    private boolean waitingForActivityResult;

    private boolean waitingForPermission;
    private PermissionsRequest pickPhotoPermissionRequest;
    private PermissionsRequest takePhotoPermissionRequest;

    public static Builder builder() {
        return new Builder();
    }

    @SuppressLint("InlinedApi")
    private ImagePickerController(final Activity activity, final Fragment fragment, Compression compression,
                                  final ErrorPresenter errorPresenter, EditActionsPresenter editActionsPresenter,
                                  String publicDirectoryName, boolean privatePhotos, String tag) {
        this.fragment = fragment;
        this.activity = activity;
        this.compression = compression;
        this.errorPresenter = errorPresenter;
        this.editActionsPresenter = editActionsPresenter;
        this.publicDirectoryName = publicDirectoryName;
        this.privatePhotos = privatePhotos;
        this.tag = tag;

        state = State.EMPTY;

        takePhotoPermissionRequest = new PermissionsRequest(
                activity != null ? activity : fragment,
                getBundleTag().hashCode() & 0x0000FFFF,
                ManifestUtils.getPermissionsForCameraApp(getActivity()));
        takePhotoPermissionRequest.setMultiplePermissionsCallback(new MultiplePermissionsCallback() {
            @Override
            public void onPermissionsResult(PermissionsRequest request, @NonNull Collection<String> granted, @NonNull Collection<String> denied) {
                if (waitingForPermission) {
                    waitingForPermission = false;
                    if (denied.isEmpty()) {
                        takePhoto();
                    }
                }
            }
        });
        attachToLifecycle(takePhotoPermissionRequest);

        pickPhotoPermissionRequest = new PermissionsRequest(
                activity != null ? activity : fragment,
                (getBundleTag().hashCode() + 1) & 0x0000FFFF,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        pickPhotoPermissionRequest.setSinglePermissionCallback(new SinglePermissionCallback() {
            @Override
            public void onPermissionGranted(PermissionsRequest request, String permission) {
                if (waitingForPermission) {
                    waitingForPermission = false;
                    pickPicture();
                }
            }

            @Override
            public void onPermissionDenied(PermissionsRequest request, String permission) {

            }
        });
        attachToLifecycle(pickPhotoPermissionRequest);

        editActionsPresenter.bindTo(this);
    }

    private Activity getActivity() {
        if (activity != null) {
            return activity;
        } else {
            return fragment.getActivity();
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setCompressionCallback(CompressionCallback compressionCallback) {
        this.compressionCallback = compressionCallback;
    }

    @Override
    protected String getBundleTag() {
        return super.getBundleTag() + tag;
    }

    private File generateTempFileOrShowError() {
        return generateTempFileOrShowError("jpg");
    }

    private File generateTempFileOrShowError(String extension) {
        return Files.generateTempFileOrShowError(getActivity(), extension, errorPresenter);
    }

    private File generatePublicTempFileOrShowError() {
        return Files.generatePublicTempFileOrShowError(getActivity(), publicDirectoryName, errorPresenter);
    }

    private String getExtensionFromCompressFormat(Bitmap.CompressFormat format) {
        switch (format) {
            case JPEG: return "jpg";
            case PNG: return "png";
            case WEBP: return "webp";

            default: return "jpg";
        }
    }

    private boolean isTempFile(Uri uri) {
        return Files.isTempFile(getActivity(), getAuthority(), uri);
    }

    private boolean deleteFile(Uri uri) {
        return Files.deleteFile(getActivity(), uri);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle stateBundle) {
        tempImageOutput = stateBundle.getParcelable(TEMP_IMAGE_OUTPUT_URI);
        waitingForActivityResult = stateBundle.getBoolean(WAITING_FOR_ACTIVITY_RESULT, false);

        state = Enums.fromBundle(State.class, stateBundle, STATE);

        imageUri = stateBundle.getParcelable(CURRENT_IMAGE_URI);
        if (imageUri != null) {
            userPickedImage = stateBundle.getBoolean(USER_PICKED_IMAGE, false);
            if (callback != null) callback.onImageUriSet(imageUri, userPickedImage);
        } else {
            userPickedImage = false;
            if (callback != null)
                //noinspection ConstantConditions
                callback.onImageUriSet(null, userPickedImage);
        }

        setState(state);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (tempImageOutput != null) {
            outState.putParcelable(TEMP_IMAGE_OUTPUT_URI, tempImageOutput);
        }
        Enums.toBundle(state, outState, STATE);
        if (imageUri != null) {
            outState.putParcelable(CURRENT_IMAGE_URI, imageUri);
            outState.putBoolean(USER_PICKED_IMAGE, userPickedImage);
        }
        outState.putBoolean(WAITING_FOR_ACTIVITY_RESULT, waitingForActivityResult);
    }

    public void onLoadingFailed(Uri imageUri) {
        if (this.imageUri == null) return;
        if (!this.imageUri.equals(imageUri)) {
            return;
        }

        if (state == State.PROCESSING) {
            setState(State.EMPTY);
            errorPresenter.showLoadingError(getActivity());
        } else {
            setState(State.ERROR);
            errorPresenter.showFileReadingError(getActivity());
        }
    }

    public void onLoadingStarted(Uri imageUri) {
        if (this.imageUri == null) return;
        if (!this.imageUri.equals(imageUri)) {
            return;
        }

        if (state != State.PROCESSING) {
            setState(State.LOADING);
        }
    }

    public void onLoadingComplete(Uri imageUri) {
        if (this.imageUri == null) return;
        if (!this.imageUri.equals(imageUri)) {
            return;
        }

        State oldState = state;
        setState(State.WITH_IMAGE);
        if (listener != null) {
            listener.onImageLoaded(imageUri);
            if (oldState == State.PROCESSING) {
                listener.onImageTaken(getImage());
            }
        }
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
        setState(state);
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setImageListener(ImageListener listener) {
        this.listener = listener;
    }

    public void setCropCallback(CropCallback cropCallback) {
        this.cropCallback = cropCallback;
    }

    public boolean isPrivatePhotos() {
        return privatePhotos;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void removeTempFiles() {
        if (isTempFile(tempImageOutput)) {
            deleteFile(tempImageOutput);
        }
        tempImageOutput = null;

        if (isTempFile(imageUri)) {
            deleteFile(imageUri);
            imageUri = null;
            userPickedImage = false;
            setState(State.EMPTY);
            if (callback != null) callback.onImageUriSet(null, userPickedImage);
        }
    }

    public Uri getImage() {
        if (state == State.WITH_IMAGE) {
            return imageUri;
        } else {
            return null;
        }
    }

    public void setImage(Uri image) {
        if (this.imageUri == null || !this.imageUri.equals(image)) {
            removeTempFiles();
        }
        if (this.imageUri == image || (this.imageUri != null && this.imageUri.equals(image))) {
            return;
        }

        this.imageUri = image;
        userPickedImage = false;
        if (image == null) {
            setState(State.EMPTY);
        } else {
            setState(State.LOADING);
            if (callback != null) callback.onImageUriSet(image, userPickedImage);
        }
    }

    public boolean hasImage() {
        return state == State.WITH_IMAGE;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public boolean hasUserImage() {
        if (state == State.WITH_IMAGE) {
            return userPickedImage;
        } else {
            return false;
        }
    }

    public void showImageFullScreen() {
        final Uri imageFile = getImage();
        if (state == State.WITH_IMAGE && imageFile != null && callback != null) {
            ImageTarget sharedImageView = callback.getImageTarget();
            ImageViewActivity.start(getActivity(), sharedImageView.getView(), imageFile);
        }
    }

    public void showEditDialog() {
        if (state == State.WITH_IMAGE) {
            if (!readonly) editActionsPresenter.showEditImageDialog();
        } else {
            if (!readonly) editActionsPresenter.showEditNotLoadedImageDialog();
        }
    }

    public void showAddDialog() {
        if (!readonly) editActionsPresenter.showAddImageDialog();
    }

    public void onTakePhotoClicked() {
        waitingForPermission = true;
        takePhotoPermissionRequest.request();
    }

    private void takePhoto() {
        Intent chooser = getTakePhotoIntentOrShowError();
        if (chooser != null) {
            startActivityForResult(chooser, REQUEST_TAKE_PHOTO);
        }
    }

    public void onPickPictureClicked() {
        waitingForPermission = true;
        pickPhotoPermissionRequest.request();
    }

    private void pickPicture() {
        startActivityForResult(Intents.getPickImageIntent(getActivity()), REQUEST_PICK);
    }

    private void startActivityForResult(Intent intent, int requestCode) {
        try {
            if (activity != null) {
                activity.startActivityForResult(intent, requestCode);
            } else {
                fragment.startActivityForResult(intent, requestCode);
            }
            waitingForActivityResult = true;
        } catch (ActivityNotFoundException ex) {
            errorPresenter.showActivityNotFoundError(getActivity());
        }
    }

    public void retryLoading() {
        if (state == State.ERROR && imageUri != null) {
            setState(State.LOADING);
            if (callback != null) callback.onImageUriSet(imageUri, userPickedImage);
        }
    }

    public void onRemoveImageClicked() {
        clear();
        if (listener != null) {
            listener.onImageRemoved();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void clear() {
        removeTempFiles();
        if (imageUri != null) {
            imageUri = null;
            userPickedImage = false;
            setState(State.EMPTY);
            if (callback != null) callback.onImageUriSet(null, userPickedImage);
        }
    }

    private void setState(State state) {
        this.state = state;
        if (callback != null) callback.onStateChanged(state);
    }

    public State getState() {
        return state;
    }

    private Intent getTakePhotoIntentOrShowError() {
        final File tempFile;
        if (privatePhotos) {
            tempFile = generateTempFileOrShowError();
        } else {
            tempFile = generatePublicTempFileOrShowError();
        }

        if (tempFile != null) {
            tempImageOutput = getUriForFile(tempFile);
            return Intents.takePhotoIntentOrShowError(tempImageOutput, getActivity(), errorPresenter);
        } else {
            tempImageOutput = null;
            return null;
        }
    }

    private Uri getUriForFile(File tempFile) {
        return SafeFileProvider.getUriForFile(getActivity(), getAuthority(), tempFile);
    }

    @NonNull
    private String getAuthority() {
        return getActivity().getPackageName() + AUTHORITY_POSTFIX;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!waitingForActivityResult) {
            return;
        }

        if (requestCode == REQUEST_PICK
                || requestCode == REQUEST_TAKE_PHOTO
                || requestCode == REQUEST_CROP) {
            waitingForActivityResult = false;
        }

        if (resultCode != Activity.RESULT_OK) {
            if (requestCode == REQUEST_CROP) {
                if (isTempFile(tempImageOutput) && !tempImageOutput.equals(imageUri)) {
                    deleteFile(tempImageOutput);
                }
            }
            return;
        }

        switch (requestCode) {
            case REQUEST_PICK:
                handleImagePick(data);
                break;
            case REQUEST_TAKE_PHOTO:
                handleTakePhoto(data);
                break;
            case REQUEST_CROP:
                handleImageCrop(data);
                break;
        }
    }

    private void handleTakePhoto(Intent data) {
        Uri dataUri = data != null ? data.getData() : null;
        final Uri imageFile;
        if (dataUri != null) {
            imageFile = getUriFromIntentResult(dataUri, privatePhotos);
        } else {
            imageFile = tempImageOutput;
        }
        if (!privatePhotos && imageFile != null) {
            MediaScannerConnection.scanFile(getActivity(),
                    new String[] { imageFile.getPath() }, new String[] {"image/jpeg"}, null);
        }
        processResultImage(imageFile);
    }

    private Uri getUriFromIntentResult(Uri resultUri, boolean copyAndRemoveSource) {
        if (resultUri == null) return null;

        if (copyAndRemoveSource && !isTempFile(resultUri)) {
            File output = generateTempFileOrShowError();
            if (output != null) {
                if (Files.copy(getActivity(), resultUri, output)) {
                    deleteFile(resultUri);
                    return getUriForFile(output);
                } else {
                    errorPresenter.showStorageError(getActivity());
                }
            }
        } else {
            return resultUri;
        }

        return null;
    }

    private void handleImagePick(Intent data) {
        Uri dataUri = data != null ? data.getData() : null;
        Uri imageFile = getUriFromIntentResult(dataUri, false);
        processResultImage(imageFile);
    }

    private void processResultImage(Uri imageUri) {
        if (imageUri == null) {
            errorPresenter.showProcessingError(getActivity());
            return;
        }

        CropOptions.Builder builder = cropCallback != null ? cropCallback.getCropOptions(imageUri) : null;
        if (builder != null) {
            File cropOutput = generateTempFileOrShowError(
                    getExtensionFromCompressFormat(builder.getCompressFormat()));
            if (cropOutput == null) {
                return;
            }

            builder.inUri(imageUri);
            builder.outUri(getUriForFile(cropOutput));
            CropOptions cropOptions = builder.build();

            startCrop(cropOptions);
            return;
        }

        processImage(imageUri, isTempFile(imageUri));
    }



    private void processImage(Uri imageUri, boolean removeInputAfter) {
        CompressionOptions compressionOptions = compressionCallback != null ? compressionCallback.getCompressionOptions(imageUri) : null;
        if (compressionOptions != null
                && compressionOptions.maxFileSize > 0
                && Files.getFileSize(getActivity(), imageUri) > compressionOptions.maxFileSize) {
            File tempImageScaled = generateTempFileOrShowError();
            if (tempImageScaled != null) {
                setState(State.PROCESSING);
                compressImageAsync(imageUri, getUriForFile(tempImageScaled), compressionOptions, removeInputAfter);
            }
        } else {
            setState(State.PROCESSING);
            onImageProcessingFinished(imageUri);
        }
    }



    private void startCrop(CropOptions cropOptions) {
        Intent intent = new Intent(getActivity(), ImagePickerConfig.getCropImageActivityClass());
        intent.putExtras(CropImageActivity.buildCropImageExtras(cropOptions));
        startActivityForResult(intent, REQUEST_CROP);
    }

    private void handleImageCrop(Intent data) {
        CropOptions cropOptions = CropImageActivity.getCropOptions(data);
        if (cropOptions == null
                || cropOptions.getOutUri() == null) {
            errorPresenter.showProcessingError(getActivity());
            return;
        }

        if (isTempFile(cropOptions.getInUri())) {
            deleteFile(cropOptions.getInUri());
        }

        processImage(cropOptions.getOutUri(), true);
    }

    private void compressImageAsync(final Uri input, final Uri output,
                                    final CompressionOptions compressionOptions,
                                    final boolean removeInputAfter) {
        new AsyncTask<Void, Void, Boolean>() {

            Activity activity = getActivity();

            @Override
            protected Boolean doInBackground(Void... params) {
                if (compression.compressImage(activity, input, output, compressionOptions)) {
                    if (removeInputAfter) {
                        deleteFile(input);
                    }
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                Activity activity = getActivity();
                if (activity != null) {
                    if (result) {
                        onImageProcessingFinished(output);
                    } else {
                        setState(State.EMPTY);
                        errorPresenter.showProcessingError(activity);
                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void onImageProcessingFinished(Uri imageUri) {
        if (isTempFile(this.imageUri)) {
            deleteFile(this.imageUri);
        }
        this.imageUri = imageUri;
        userPickedImage = true;
        if (callback != null) //noinspection ConstantConditions
            callback.onImageUriSet(imageUri, userPickedImage);
    }

    public interface ImageListener {
        void onImageTaken(Uri image);
        void onImageLoaded(Uri image);
        void onImageRemoved();
    }

    public interface Callback {
        void onImageUriSet(@Nullable Uri imageUri, boolean userPickedImage);
        void onStateChanged(State state);
        ImageTarget getImageTarget();
    }

    public interface CompressionCallback {
        @Nullable CompressionOptions getCompressionOptions(@NonNull Uri imageUri);
    }

    public interface CropCallback {
        @Nullable CropOptions.Builder getCropOptions(@NonNull Uri imageUri);
    }

    public enum State {
        EMPTY,
        PROCESSING,
        LOADING,
        WITH_IMAGE,
        ERROR,
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static final class Builder {
        private Activity activity;
        private Fragment fragment;
        private ErrorPresenter errorPresenter;
        private EditActionsPresenter editActionsPresenter;
        private Compression compression;
        private String tag;
        private String publicDirectoryName;
        private boolean privatePhotos = true;

        private Builder() {
        }

        public Builder activity(Activity activity) {
            this.activity = activity;
            return this;
        }

        public Builder fragment(Fragment fragment) {
            this.fragment = fragment;
            return this;
        }

        public Builder errorPresenter(ErrorPresenter errorPresenter) {
            this.errorPresenter = errorPresenter;
            return this;
        }

        public Builder compression(Compression compression) {
            this.compression = compression;
            return this;
        }

        public Builder editActionsPresenter(EditActionsPresenter editActionsPresenter) {
            this.editActionsPresenter = editActionsPresenter;
            return this;
        }

        public Builder publicDirectoryName(String publicDirectoryName) {
            this.publicDirectoryName = publicDirectoryName;
            return this;
        }

        public Builder privatePhotos(boolean privatePhotos) {
            this.privatePhotos = privatePhotos;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public ImagePickerController build() {
            if (activity == null && fragment == null
                    || (activity != null && fragment != null)) {
                throw new NullPointerException("Please provide either activity or fragment");
            }
            if (editActionsPresenter == null) {
                throw new NullPointerException("Please provide editActionsPresenter");
            }
            if (tag == null) {
                tag = "default";
            }
            if (publicDirectoryName == null) {
                publicDirectoryName = "ImagePicker";
            }
            if (errorPresenter == null) {
                errorPresenter = new ToastErrorPresenter();
            }
            if (compression == null) {
                compression = new DefaultCompression();
            }
            return new ImagePickerController(activity, fragment, compression, errorPresenter, editActionsPresenter,
                    publicDirectoryName, privatePhotos, tag);
        }
    }
}