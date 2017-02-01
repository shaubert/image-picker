package com.shaubert.ui.imagepicker;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

class Files {

    public static final String TAG = Files.class.getSimpleName();

    private static final SimpleDateFormat PUBLIC_FILE_NAME_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("[^\\p{L}0-9_\\.]");

    //Bound to xml/path.xml
    private static final String IMAGE_PICKER_TEMP_DIR_NAME = "image-picker-temp";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File generateTempFileOrShowError(Context context, ErrorPresenter errorPresenter) {
        File file = generateTempFile(context);
        if (file == null) {
            errorPresenter.showStorageError(context);
        }
        return file;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File generatePublicTempFileOrShowError(Context context, String dirName, ErrorPresenter errorPresenter) {
        File file = generatePublicTempFile(dirName);
        if (file == null) {
            errorPresenter.showStorageError(context);
        }
        return file;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File generateTempFile(Context context) {
        File tempRoot = getTempRoot(context);
        if (tempRoot != null) {
            return new File(tempRoot, UUID.randomUUID().toString());
        } else {
            return null;
        }
    }

    public static File getTempRoot(Context context) {
        File photosDir = StorageUtils.getCacheDirectory(context);
        if (photosDir != null) {
            File innerDir = new File(photosDir, IMAGE_PICKER_TEMP_DIR_NAME);
            if (innerDir.exists() || innerDir.mkdirs()) {
                return innerDir;
            }
        }
        return null;
    }

    public static boolean isTempFile(Context context, File file) {
        File tempRoot = getTempRoot(context);
        if (file == null || tempRoot == null) return false;
        return file.getAbsolutePath().contains(tempRoot.getAbsolutePath());
    }

    public static boolean isPublicFile(File file) {
        File photosDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (photosDir == null || file == null) return false;
        return file.getAbsolutePath().contains(photosDir.getAbsolutePath());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File generatePublicTempFile(String dirName) {
        File externalStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (externalStorage == null) return null;

        File appPhotosDir = TextUtils.isEmpty(dirName) ? externalStorage : new File(externalStorage, dirName);
        if (!appPhotosDir.exists() && !appPhotosDir.mkdirs()) {
            return null;
        }

        String filename = "IMG_" + PUBLIC_FILE_NAME_FORMAT.format(new Date());
        File resultFile;
        int i = 1;
        while (true) {
            resultFile = new File(appPhotosDir, filename + (i == 1 ? "" : ("_" + i)) + ".jpg");
            if (!resultFile.exists()) break;
            i++;
        }

        return resultFile;
    }

    public static boolean copy(File from, File to) {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            to.getParentFile().mkdirs();
            inputStream = new FileInputStream(from);
            outputStream = new FileOutputStream(to);
            byte[] buffer = new byte[1024 * 50];
            int len;
            while ((len = inputStream.read(buffer)) >= 0) {
                if (len > 0) {
                    outputStream.write(buffer, 0, len);
                }
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "failed to copy file", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return false;
    }


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {
        try {
            return getPathInternal(context, uri);
        } catch (Exception ex) {
            Log.e(TAG, "failed to get path", ex);
            return null;
        }
    }

    //get from there https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getPathInternal(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}