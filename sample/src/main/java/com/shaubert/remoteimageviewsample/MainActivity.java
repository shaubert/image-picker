package com.shaubert.remoteimageviewsample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;
import com.shaubert.lifecycle.objects.dispatchers.support.LifecycleDispatcherAppCompatActivity;
import com.shaubert.ui.imagepicker.*;
import com.shaubert.ui.imagepicker.nostra.ViewScaleType;

import java.io.File;


public class MainActivity extends LifecycleDispatcherAppCompatActivity {

    private ActivityMainHolder viewHolder;

    @Override
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewHolder = new ActivityMainHolder(this);

        ImagePicker imagePicker1 = new ImagePicker(this, "picker");
        imagePicker1.setDefaultImageUrl("http://sipi.usc.edu/database/preview/misc/4.2.05.png");
        viewHolder.getImage3().setOnClickListener(imagePicker1.createImageClickListener());
        imagePicker1.setupViews(new ImageViewTarget(viewHolder.getImage3()), viewHolder.getLoad3Button(), viewHolder.getImage3Progress(), viewHolder.getImage3Error());
        imagePicker1.setCompressionOptions(CompressionOptions.newBuilder()
                .maxFileSize(1024 * 200)
                .targetHeight(512)
                .targetWidth(512)
                .targetScaleType(ViewScaleType.CROP)
                .build());
        attachToLifecycle(imagePicker1);

        ImagePicker imagePicker2 = new ImagePicker(this, "cropper");
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
        imagePicker2.setPrivatePhotos(true);
        attachToLifecycle(imagePicker2);

        final ImagePicker imagePicker3 = new ImagePicker(this, "picker-3");
        imagePicker3.setCompressionOptions(CompressionOptions.newBuilder()
                .maxFileSize(1024 * 200)
                .targetScaleType(ViewScaleType.CROP)
                .build());
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


        ImagePicker imagePicker4 = new ImagePicker(this, "picker-4");
        viewHolder.getImage6().setOnClickListener(imagePicker4.createImageClickListener());
        imagePicker4.setupViews(new ImageViewTarget(viewHolder.getImage6()), viewHolder.getLoad6Button(), null, null);
        imagePicker4.setPrivatePhotos(true);
        imagePicker4.setImageUrl("http://sipi.usc.edu/database/preview/misc/4.2.06.png");
        attachToLifecycle(imagePicker4);
    }

}
