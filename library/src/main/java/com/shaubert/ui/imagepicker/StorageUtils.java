package com.shaubert.ui.imagepicker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
final class StorageUtils {

	private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";

	private StorageUtils() {
	}

	/**
	 * Returns application cache directory. Cache directory will be created on SD card
	 * <i>("/Android/data/[app_package_name]/cache")</i> if card is mounted and app has appropriate permission. Else -
	 * Android defines cache directory on device's file system.
	 *
	 * @param context Application context
	 * @return Cache {@link File directory}.<br />
	 * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
	 * {@link android.content.Context#getCacheDir() Context.getCacheDir()} returns null).
	 */
	public static File getCacheDirectory(Context context) {
		return getCacheDirectory(context, true);
	}

	/**
	 * Returns application cache directory. Cache directory will be created on SD card
	 * <i>("/Android/data/[app_package_name]/cache")</i> (if card is mounted and app has appropriate permission) or
	 * on device's file system depending incoming parameters.
	 *
	 * @param context        Application context
	 * @param preferExternal Whether prefer external location for cache
	 * @return Cache {@link File directory}.<br />
	 * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
	 * {@link android.content.Context#getCacheDir() Context.getCacheDir()} returns null).
	 */
	public static File getCacheDirectory(Context context, boolean preferExternal) {
		File appCacheDir = null;
		String externalStorageState;
		try {
			externalStorageState = Environment.getExternalStorageState();
		} catch (NullPointerException e) { // (sh)it happens (Issue #660)
			externalStorageState = "";
		} catch (IncompatibleClassChangeError e) { // (sh)it happens too (Issue #989)
			externalStorageState = "";
		}
		if (preferExternal && MEDIA_MOUNTED.equals(externalStorageState) && hasExternalStoragePermission(context)) {
			appCacheDir = getExternalCacheDir(context);
		}
		if (appCacheDir == null) {
			appCacheDir = context.getCacheDir();
		}
		if (appCacheDir == null) {
			return context.getFilesDir();
		}
		return appCacheDir;
	}

	private static File getExternalCacheDir(Context context) {
		File appCacheDir = context.getExternalCacheDir();
		if (appCacheDir != null && !appCacheDir.exists()) {
			if (!appCacheDir.mkdirs()) {
				return null;
			}
			try {
				new File(appCacheDir, ".nomedia").createNewFile();
			} catch (IOException e) {
			}
		}
		return appCacheDir;
	}

	private static boolean hasExternalStoragePermission(Context context) {
		int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
		return perm == PackageManager.PERMISSION_GRANTED;
	}

}