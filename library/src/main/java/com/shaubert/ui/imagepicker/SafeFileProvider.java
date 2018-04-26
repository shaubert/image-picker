package com.shaubert.ui.imagepicker;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

class SafeFileProvider {

    /**
     * @param context   context
     * @param authority authority
     * @param file      file
     * @return uri for file
     */
    static Uri getUriForFile(Context context, String authority, File file) {
        try {
            return FileProvider.getUriForFile(new HuaweiFixedContext(context), authority, file);
        } catch (IllegalArgumentException ignored) {
            return Uri.fromFile(file);
        }
    }

    /**
     * Many Huawei device models break the Android contract for calls to ContextCompat#getExternalFilesDirs(String).
     * Rather than returning Context#getExternalFilesDir(String) (ie the default entry) as the first object in the
     * array, they instead return the first object as the path to the external SD card, if one is present.
     * <p>
     * By breaking this ordering contract, these Huawei devices with external SD cards will crash with an
     * IllegalArgumentException on calls to FileProvider#getUriForFile(Context, String, File) for
     * external-files-path roots. Here we try to fix this order.
     */
    private static class HuaweiFixedContext extends ContextWrapper {

        public HuaweiFixedContext(Context base) {
            super(base);
        }

        @Override
        public File[] getExternalFilesDirs(String type) {
            File[] dirs = super.getExternalFilesDirs(type);
            File defaultDir = super.getExternalFilesDir(type);
            if (dirs.length > 0
                    && defaultDir != null
                    && !defaultDir.equals(dirs[0])) {
                File[] newDirs = new File[dirs.length + 1];
                newDirs[0] = defaultDir;
                System.arraycopy(dirs, 0, newDirs, 1, dirs.length);
                dirs = newDirs;
            }
            return dirs;
        }
    }

}
