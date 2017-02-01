package com.shaubert.ui.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import com.shaubert.lifecycle.objects.LifecycleObjectsGroup;
import com.shaubert.m.permission.MultiplePermissionsCallback;
import com.shaubert.m.permission.PermissionsRequest;

import java.io.File;
import java.util.Collection;

public class ImagePickerController extends LifecycleObjectsGroup {

    private static final String TEMP_IMAGE_OUTPUT_FILE_NAME = "__sh_image_picker_temp_image_output_file_name";
    private static final String CURRENT_IMAGE_FILE = "__sh_image_picker_current_image_file_extra";
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
    private File imageFile;
    private File tempImageOutput;

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

    private PermissionsRequest takePhotoPermissionRequest;

    public static Builder builder() {
        return new Builder();
    }

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
        return Files.generateTempFileOrShowError(getActivity(), errorPresenter);
    }

    private File generatePublicTempFileOrShowError() {
        return Files.generatePublicTempFileOrShowError(getActivity(), publicDirectoryName, errorPresenter);
    }

    private boolean isTempFile(File file) {
        return Files.isTempFile(getActivity(), file);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle stateBundle) {
        String path = stateBundle.getString(TEMP_IMAGE_OUTPUT_FILE_NAME);
        if (!TextUtils.isEmpty(path)) {
            tempImageOutput = new File(path);
        } else {
            tempImageOutput = null;
        }
        waitingForActivityResult = stateBundle.getBoolean(WAITING_FOR_ACTIVITY_RESULT, false);

        state = Enums.fromBundle(State.class, stateBundle, STATE);

        String imagePath = stateBundle.getString(CURRENT_IMAGE_FILE);
        if (!TextUtils.isEmpty(imagePath)) {
            imageFile = new File(imagePath);
            userPickedImage = stateBundle.getBoolean(USER_PICKED_IMAGE, false);
            if (callback != null) callback.onImageFileSet(imageFile);
        } else {
            imageFile = null;
            userPickedImage = false;
            if (callback != null) callback.onImageFileSet(null);
        }

        setState(state);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (tempImageOutput != null) {
            outState.putString(TEMP_IMAGE_OUTPUT_FILE_NAME, tempImageOutput.getAbsolutePath());
        }
        Enums.toBundle(state, outState, STATE);
        if (imageFile != null) {
            outState.putString(CURRENT_IMAGE_FILE, imageFile.getAbsolutePath());
            outState.putBoolean(USER_PICKED_IMAGE, userPickedImage);
        }
        outState.putBoolean(WAITING_FOR_ACTIVITY_RESULT, waitingForActivityResult);
    }

    public void onLoadingFailed(String imageUri) {
        if (imageFile == null) return;
        if (!Scheme.FILE.wrap(imageFile.getAbsolutePath()).equals(imageUri)) {
            return;
        }

        if (state == State.PROCESSING) {
            setState(State.EMPTY);
            errorPresenter.showLoadingError(getActivity());
        } else {
            setState(State.ERROR);
        }
    }

    public void onLoadingStarted(String imageUri) {
        if (imageFile == null) return;
        if (!Scheme.FILE.wrap(imageFile.getAbsolutePath()).equals(imageUri)) {
            return;
        }

        if (state != State.PROCESSING) {
            setState(State.LOADING);
        }
    }

    public void onLoadingComplete(String imageUri) {
        if (imageFile == null) return;
        if (!Scheme.FILE.wrap(imageFile.getAbsolutePath()).equals(imageUri)) {
            return;
        }

        State oldState = state;
        setState(State.WITH_IMAGE);
        if (listener != null) {
            listener.onImageLoaded(imageUri);
            if (oldState == State.PROCESSING) {
                listener.onImageTaken(getImageFile());
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
            tempImageOutput.delete();
        }
        tempImageOutput = null;

        if (isTempFile(imageFile)) {
            imageFile.delete();
            imageFile = null;
            userPickedImage = false;
            setState(State.EMPTY);
            if (callback != null) callback.onImageFileSet(null);
        }
    }

    public File getImageFile() {
        if (state == State.WITH_IMAGE) {
            return imageFile;
        } else {
            return null;
        }
    }

    public void setImageFile(File imageFile) {
        if (this.imageFile == null || !this.imageFile.equals(imageFile)) {
            removeTempFiles();
        }
        if (this.imageFile == imageFile || (this.imageFile != null && this.imageFile.equals(imageFile))) {
            return;
        }

        this.imageFile = imageFile;
        userPickedImage = false;
        if (imageFile == null) {
            setState(State.EMPTY);
        } else {
            setState(State.LOADING);
            if (callback != null) callback.onImageFileSet(imageFile);
        }
    }

    public Uri getImageUri() {
        File imageFile = getImageFile();
        if (imageFile != null) {
            return Uri.fromFile(imageFile);
        } else {
            return null;
        }
    }

    public String getImageUrl() {
        File imageFile = getImageFile();
        if (imageFile != null) {
            return Scheme.FILE.wrap(imageFile.getAbsolutePath());
        } else {
            return null;
        }
    }

    public boolean hasImage() {
        return state == State.WITH_IMAGE;
    }

    public boolean hasUserImage() {
        if (state == State.WITH_IMAGE) {
            return userPickedImage;
        } else {
            return false;
        }
    }

    public void showImageFullScreen() {
        final File imageFile = getImageFile();
        if (state == State.WITH_IMAGE && imageFile != null && callback != null) {
            ImageTarget sharedImageView = callback.getImageTarget();
            ImageViewActivity.start(getActivity(), sharedImageView.getView(), getImageUrl());
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
        startActivityForResult(Intents.getPickImageIntent(getActivity()), REQUEST_PICK);
    }

    private void startActivityForResult(Intent intent, int requestCode) {
        waitingForActivityResult = true;
        if (activity != null) {
            activity.startActivityForResult(intent, requestCode);
        } else {
            fragment.startActivityForResult(intent, requestCode);
        }
    }

    public void retryLoading() {
        if (state == State.ERROR && imageFile != null) {
            setState(State.LOADING);
            if (callback != null) callback.onImageFileSet(imageFile);
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
        if (imageFile != null) {
            imageFile = null;
            userPickedImage = false;
            setState(State.EMPTY);
            if (callback != null) callback.onImageFileSet(null);
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
        if (privatePhotos) {
            tempImageOutput = generateTempFileOrShowError();
        } else {
            tempImageOutput = generatePublicTempFileOrShowError();
        }

        if (tempImageOutput != null) {
            return Intents.takePhotoIntent(tempImageOutput, getActivity().getPackageName() + AUTHORITY_POSTFIX, getActivity());
        } else {
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!waitingForActivityResult) {
            return;
        }

        waitingForActivityResult = false;
        if (resultCode != Activity.RESULT_OK) {
            if (requestCode == REQUEST_CROP) {
                if (isTempFile(tempImageOutput) && !tempImageOutput.equals(imageFile)) {
                    //noinspection ResultOfMethodCallIgnored
                    tempImageOutput.delete();
                    tempImageOutput = null;
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
        final File imageFile;
        if (dataUri != null) {
            imageFile = getFileFromIntentResult(dataUri, privatePhotos);
        } else {
            imageFile = tempImageOutput;
        }
        if (!privatePhotos && imageFile != null) {
            MediaScannerConnection.scanFile(getActivity(),
                    new String[] { imageFile.getAbsolutePath() }, new String[] {"image/jpeg"}, null);
        }
        processResultImage(imageFile);
    }

    private File getFileFromIntentResult(Uri resultUri, boolean copyAndRemoveSource) {
        if (resultUri == null) return null;

        String path = Files.getPath(getActivity(), resultUri);
        if (TextUtils.isEmpty(path)) {
            errorPresenter.showFileReadingError(getActivity());
        } else {
            File source = new File(path);
            if (copyAndRemoveSource && !isTempFile(source)) {
                File output = generateTempFileOrShowError();
                if (output != null) {
                    if (Files.copy(source, output)) {
                        //noinspection ResultOfMethodCallIgnored
                        source.delete();
                        return output;
                    } else {
                        errorPresenter.showStorageError(getActivity());
                    }
                }
            } else {
                return source;
            }
        }
        return null;
    }

    private void handleImagePick(Intent data) {
        Uri dataUri = data != null ? data.getData() : null;
        File imageFile = getFileFromIntentResult(dataUri, false);
        processResultImage(imageFile);
    }

    private void processResultImage(File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            errorPresenter.showProcessingError(getActivity());
            return;
        }

        if (cropCallback != null && cropCallback.shouldCrop(imageFile)) {
            File cropOutput = generateTempFileOrShowError();
            if (cropOutput == null) {
                return;
            }

            CropOptions.Builder builder = CropOptions.newBuilder();
            cropCallback.setupCropOptions(imageFile, builder);
            builder.inUri(Uri.fromFile(imageFile));
            builder.outUri(Uri.fromFile(cropOutput));
            CropOptions cropOptions = builder.build();

            startCrop(cropOptions);
            return;
        }

        processImage(imageFile, isTempFile(imageFile));
    }

    private void processImage(File imageFile, boolean removeInputAfter) {
        CompressionOptions compressionOptions = compressionCallback != null ? compressionCallback.getCompressionOptions(imageFile) : null;
        if (compressionOptions != null
                && compressionOptions.maxFileSize > 0
                && imageFile.length() > compressionOptions.maxFileSize) {
            File tempImageScaled = generateTempFileOrShowError();
            if (tempImageScaled != null) {
                setState(State.PROCESSING);
                compressImageAsync(imageFile, tempImageScaled, compressionOptions, removeInputAfter);
            }
        } else {
            setState(State.PROCESSING);
            onImageProcessingFinished(imageFile);
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

        String inPath = Files.getPath(getActivity(), cropOptions.getInUri());
        if (!TextUtils.isEmpty(inPath)) {
            File inFile = new File(inPath);
            if (isTempFile(inFile)) {
                //noinspection ResultOfMethodCallIgnored
                inFile.delete();
            }
        }

        String outPath = Files.getPath(getActivity(), cropOptions.getOutUri());
        if (!TextUtils.isEmpty(outPath)) {
            File croppedImageFile = new File(outPath);
            processImage(croppedImageFile, true);
        } else {
            errorPresenter.showProcessingError(getActivity());
        }
    }

    private void compressImageAsync(final File input, final File output,
                                    final CompressionOptions compressionOptions,
                                    final boolean removeInputAfter) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if (compression.compressImage(input, output, compressionOptions)) {
                    if (removeInputAfter) {
                        //noinspection ResultOfMethodCallIgnored
                        input.delete();
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

    private void onImageProcessingFinished(File imageFile) {
        if (isTempFile(this.imageFile)) {
            //noinspection ResultOfMethodCallIgnored
            this.imageFile.delete();
        }
        this.imageFile = imageFile;
        userPickedImage = true;
        if (callback != null) callback.onImageFileSet(imageFile);
    }

    public interface ImageListener {
        void onImageTaken(File imageFile);
        void onImageLoaded(String imageUri);
        void onImageRemoved();
    }

    public interface Callback {
        void onImageFileSet(@Nullable File imageFile);
        void onStateChanged(State state);
        ImageTarget getImageTarget();
    }

    public interface CompressionCallback {
        @Nullable CompressionOptions getCompressionOptions(@NonNull File imageFile);
    }

    public interface CropCallback {
        boolean shouldCrop(@NonNull File imageFile);
        void setupCropOptions(@NonNull File imageFile, @NonNull CropOptions.Builder builder);
    }

    public enum State {
        EMPTY,
        PROCESSING,
        LOADING,
        WITH_IMAGE,
        ERROR,
    }

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