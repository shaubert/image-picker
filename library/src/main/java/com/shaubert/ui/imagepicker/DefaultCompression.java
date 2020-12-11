package com.shaubert.ui.imagepicker;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.*;

public class DefaultCompression implements Compression {

    private static final String TAG = DefaultCompression.class.getSimpleName();

    @Override
    public boolean compressImage(Context context, Uri source, Uri destination, CompressionOptions compressionOptions) {
        return compressImage(context, source, destination, compressionOptions.targetWidth,
                compressionOptions.targetHeight, compressionOptions.maxFileSize);
    }

    private boolean compressImage(Context context, Uri source, Uri destination, int targetWidth, int targetHeight, long maxSize) {
        ContentResolver contentResolver = context.getContentResolver();
        InputStream boundsStream = null;
        InputStream is = null;
        OutputStream os = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            boundsStream = contentResolver.openInputStream(source);
            BitmapFactory.decodeStream(boundsStream, null, options);
            int w = options.outWidth;
            int h = options.outHeight;
            if (w > 0 && h > 0) {
                options.inSampleSize = ImageSizeUtils.computeImageSampleSize(new ImageSize(w, h),
                        new ImageSize(targetWidth, targetHeight), true);
            }
            options.inJustDecodeBounds = false;
            is = contentResolver.openInputStream(source);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            int quality = 100;
            long currentSize;
            do {
                quality -= 10;
                os = contentResolver.openOutputStream(destination, "rw");
                if (os == null) throw new FileNotFoundException(destination.toString());
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
                os.close();
                currentSize = Files.getFileSize(context, destination);
            } while (quality > 50
                    && currentSize > maxSize
                    && maxSize > 0);
            bitmap.recycle();
            copyExif(context, source, destination);
            return true;
        } catch (IOException e) {
            DebugLog.logError("failed to create compressed image", e);
        } finally {
            if (boundsStream != null) {
                try {
                    boundsStream.close();
                } catch (IOException ignored) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignored) {
                }
            }
        }
        return false;
    }

    @SuppressWarnings("WeakerAccess")
    public static void copyExif(Context context, Uri source, Uri destination) {
        if (Build.VERSION.SDK_INT >= 24) {
            copyExif_V24(context, source, destination);
        } else {
            copyExif_Old(context, source, destination);
        }
    }

    @SuppressWarnings("deprecation")
    private static void copyExif_Old(Context context, Uri source, Uri destination) {
        try {
            ExifInterface oldExif = new ExifInterface(Files.getPath(context, source));
            String exifOrientation = oldExif.getAttribute(ExifInterface.TAG_ORIENTATION);
            if (exifOrientation != null) {
                ExifInterface newExif = new ExifInterface(Files.getPath(context, destination));
                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation);
                newExif.saveAttributes();
            }
        } catch (Exception ex) {
            Log.d(TAG, "failed to copy exif", ex);
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static void copyExif_V24(Context context, Uri source, Uri destination) {
        InputStream sourceStream = null;
        OutputStream destStream = null;
        try {
            sourceStream = context.getContentResolver().openInputStream(source);
            ExifInterface oldExif = new ExifInterface(sourceStream);
            String exifOrientation = oldExif.getAttribute(ExifInterface.TAG_ORIENTATION);
            if (exifOrientation != null) {
                destStream = context.getContentResolver().openOutputStream(destination, "rw");
                if (destStream instanceof FileOutputStream) {
                    final ExifInterface newExif = new ExifInterface(((FileOutputStream) destStream).getFD());
                    newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation);
                    newExif.saveAttributes();
                } else {
                    throw new IllegalStateException("not file stream");
                }
            }
        } catch (Exception ex) {
            Log.d(TAG, "failed to copy exif", ex);
            copyExif_Old(context, source, destination);
        } finally {
            try {
                if (sourceStream != null) sourceStream.close();
            } catch (IOException ignored) {
            }
            try {
                if (destStream != null) destStream.close();
            } catch (IOException ignored) {
            }
        }
    }

}
