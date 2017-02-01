package com.shaubert.imagepickersample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;
import com.shaubert.lifecycle.objects.dispatchers.support.LifecycleDispatcherAppCompatActivity;
import com.shaubert.ui.imagepicker.*;

import java.io.File;


public class MainActivity extends LifecycleDispatcherAppCompatActivity {

    @Override
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityMainHolder viewHolder = new ActivityMainHolder(this);

        ImagePicker imagePicker1 = new ImagePicker(
                ImagePickerController.builder()
                        .activity(this)
                        .editActionsPresenter(new EditActionsDialogPresenter(this, getSupportFragmentManager(), "picker-1"))
                        .privatePhotos(false)
                        .tag("picker-1")
                        .build(),
                "picker");
        imagePicker1.setDefaultImageUrl("http://sipi.usc.edu/database/preview/misc/4.2.05.png");
        viewHolder.getImage3().setOnClickListener(imagePicker1.createImageClickListener());
        imagePicker1.setupViews(new ImageViewTarget(viewHolder.getImage3()), viewHolder.getLoad3Button(), viewHolder.getImage3Progress(), viewHolder.getImage3Error());
        imagePicker1.setCompressionCallback(new ImagePickerController.CompressionCallback() {
            @Nullable
            @Override
            public CompressionOptions getCompressionOptions(@NonNull File imageFile) {
                return CompressionOptions.newBuilder()
                        .maxFileSize(1024 * 200)
                        .targetHeight(512)
                        .targetWidth(512)
                        .build();
            }
        });
        attachToLifecycle(imagePicker1);

        ImagePicker imagePicker2 = new ImagePicker(
                ImagePickerController.builder()
                        .activity(this)
                        .editActionsPresenter(new EditActionsDialogPresenter(this, getSupportFragmentManager(), "picker-2"))
                        .privatePhotos(true)
                        .tag("picker-2")
                        .build(),
                "cropper");
        viewHolder.getImage4().setOnClickListener(imagePicker2.createImageClickListener());
        imagePicker2.setupViews(new ImageViewTarget(viewHolder.getImage4()), viewHolder.getLoad4Button(), null, null);
        imagePicker2.setCropCallback(new ImagePickerController.CropCallback() {
            @Override
            public boolean shouldCrop(@NonNull File imageFile) {
                return true;
            }

            @Override
            public void setupCropOptions(@NonNull File imageFile, @NonNull CropOptions.Builder builder) {
                builder.minHeight(200)
                        .minWidth(200)
                        .maxHeight(400)
                        .maxWidth(400)
                        .aspectX(1)
                        .aspectY(2);
            }
        });
        attachToLifecycle(imagePicker2);

        final ImagePicker imagePicker3 = new ImagePicker(
                ImagePickerController.builder()
                        .activity(this)
                        .editActionsPresenter(new EditActionsDialogPresenter(this, getSupportFragmentManager(), "picker-3"))
                        .privatePhotos(false)
                        .tag("picker-3")
                        .build(),
                "picker-3");
        imagePicker3.setCompressionCallback(new ImagePickerController.CompressionCallback() {
            @Nullable
            @Override
            public CompressionOptions getCompressionOptions(@NonNull File imageFile) {
                return CompressionOptions.newBuilder()
                        .maxFileSize(1024 * 200)
                        .build();
            }
        });
        viewHolder.getLoad5Button().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePicker3.showAddDialog();
            }
        });
        imagePicker3.setListener(new ImagePickerController.ImageListener() {
            @Override
            public void onImageTaken(File imageFile) {
                Toast.makeText(MainActivity.this,
                        "Image taken: " + imageFile.getName() +
                                "\nSize: " + imageFile.length() / (1024) + "KB",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onImageLoaded(String imageUri) {
            }

            @Override
            public void onImageRemoved() {
            }
        });
        attachToLifecycle(imagePicker3);


        ImagePicker imagePicker4 = new ImagePicker(
                ImagePickerController.builder()
                        .activity(this)
                        .editActionsPresenter(new EditActionsDialogPresenter(this, getSupportFragmentManager(), "picker-4"))
                        .privatePhotos(true)
                        .tag("picker-4")
                        .build(),
                "picker-4");
        viewHolder.getImage6().setOnClickListener(imagePicker4.createImageClickListener());
        imagePicker4.setupViews(new ImageViewTarget(viewHolder.getImage6()), viewHolder.getLoad6Button(), null, null);
        imagePicker4.setImageUrl("http://sipi.usc.edu/database/preview/misc/4.2.06.png");
        attachToLifecycle(imagePicker4);
    }

}
