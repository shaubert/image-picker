package com.shaubert.ui.imagepicker;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;

class SafeFileProvider {

    /**
     * Many Huawei device models break the Android contract for calls to ContextCompat#getExternalFilesDirs(String).
     * Rather than returning Context#getExternalFilesDir(String) (ie the default entry) as the first object in the
     * array, they instead return the first object as the path to the external SD card, if one is present.
     *
     * By breaking this ordering contract, these Huawei devices with external SD cards will crash with an
     * IllegalArgumentException on calls to FileProvider#getUriForFile(Context, String, File) for
     * external-files-path roots. Here is the easiest approach to be to just catch this issue
     * and return Uri#fromFile(File).
     * @param context context
     * @param authority authority
     * @param file file
     * @return uri for file
     */
    static Uri getUriForFile(Context context, String authority, File file) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            try {
                return FileProvider.getUriForFile(context, authority, file);
            } catch (IllegalArgumentException e) {
                return Uri.fromFile(file);
            }
        } else {
            return FileProvider.getUriForFile(context, authority, file);
        }
    }

}
