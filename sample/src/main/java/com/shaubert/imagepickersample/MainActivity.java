package com.shaubert.imagepickersample;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;
import com.shaubert.lifecycle.objects.dispatchers.support.LifecycleDispatcherAppCompatActivity;
import com.shaubert.ui.imagepicker.*;


public class MainActivity extends LifecycleDispatcherAppCompatActivity {

    @Override
    @SuppressWarnings("deprecation")
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
        imagePicker1.setDefaultImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        viewHolder.getImage3().setOnClickListener(imagePicker1.createImageClickListener());
        imagePicker1.setupViews(new ImageViewTarget(viewHolder.getImage3()), viewHolder.getLoad3Button(), viewHolder.getImage3Progress(), viewHolder.getImage3Error());
        imagePicker1.setCompressionCallback(new ImagePickerController.CompressionCallback() {
            @Nullable
            @Override
            public CompressionOptions getCompressionOptions(@NonNull Uri imageUri) {
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
            @Nullable
            @Override
            public CropOptions.Builder getCropOptions(@NonNull Uri imageUri) {
                return CropOptions.newBuilder()
                        .minHeight(50)
                        .minWidth(50)
                        .maxHeight(400)
                        .maxWidth(400)
                        .aspectX(1)
                        .aspectY(1)
                        .bottomInfoLayout(R.layout.crop_bottom_info_sample);
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
            public CompressionOptions getCompressionOptions(@NonNull Uri imageUri) {
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
            public void onImageTaken(Uri image) {
                Toast.makeText(MainActivity.this,
                        "Image taken: " + image.toString(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onImageLoaded(Uri image) {

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
        imagePicker4.setImage(Uri.parse("http://sipi.usc.edu/database/preview/misc/4.2.06.png"));
        attachToLifecycle(imagePicker4);
    }

}
