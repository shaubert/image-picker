package com.shaubert.ui.imagepicker;

import android.content.Context;
import android.widget.Toast;

public class ToastErrorPresenter implements ErrorPresenter {

    public void showProcessingError(Context context) {
        Toast.makeText(context, R.string.sh_image_picker_take_photo_processing_error, Toast.LENGTH_SHORT).show();
    }

    public void showLoadingError(Context context) {
        Toast.makeText(context, R.string.sh_image_picker_take_photo_loading_error, Toast.LENGTH_SHORT).show();
    }

    public void showStorageError(Context context) {
        Toast.makeText(context, R.string.sh_image_picker_storage_not_available_message, Toast.LENGTH_SHORT).show();
    }

    public void showFileReadingError(Context context) {
        Toast.makeText(context, R.string.sh_image_picker_unable_to_read_file_message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showActivityNotFoundError(Context context) {
        Toast.makeText(context, R.string.sh_image_picker_unable_to_start_intent, Toast.LENGTH_SHORT).show();
    }

}
