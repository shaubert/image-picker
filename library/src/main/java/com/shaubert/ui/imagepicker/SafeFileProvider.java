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
        HuaweiFixedContext fixedContext = new HuaweiFixedContext(context);
        while (true) {
            try {
                return FileProvider.getUriForFile(context, authority, file);
            } catch (IllegalArgumentException e) {
                fixedContext.placeDirAtTop++;
                if (fixedContext.placeDirAtTop >= fixedContext.lastDirsCount) {
                    break;
                }
            }
        }
        return Uri.fromFile(file);
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
        int placeDirAtTop;
        int lastDirsCount;

        public HuaweiFixedContext(Context base) {
            super(base);
        }

        @Override
        public File[] getExternalFilesDirs(String type) {
            File[] dirs = super.getExternalFilesDirs(type);
            lastDirsCount = dirs.length;
            if (dirs.length > 1 && dirs.length > placeDirAtTop) {
                File temp = dirs[0];
                dirs[0] = dirs[placeDirAtTop];
                dirs[placeDirAtTop] = temp;
            }
            return dirs;
        }
    }

}
