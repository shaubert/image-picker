package com.shaubert.ui.imagepicker;

import android.content.Context;

public interface ErrorPresenter {

    void showProcessingError(Context context);

    void showLoadingError(Context context);

    void showStorageError(Context context);

    void showFileReadingError(Context context);

    void showActivityNotFoundError(Context context);

}
