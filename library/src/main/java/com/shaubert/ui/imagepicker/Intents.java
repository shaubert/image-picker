package com.shaubert.ui.imagepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import java.util.List;

import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

class Intents {

    public static Intent getPickImageIntent(Context context) {
        Intent intent = Intents.pickImageIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return intent;
        } else {
            return Intent.createChooser(intent, context.getString(R.string.sh_image_picker_take_picture_chooser_title));
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static Intent pickImageIntent() {
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return new Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("image/*")
                    .setFlags(flags);
        } else {
            return new Intent(Intent.ACTION_GET_CONTENT)
                    .setType("image/*")
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setFlags(flags);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static Intent takePhotoIntentOrShowError(@NonNull Uri output,
                                                    Context context,
                                                    ErrorPresenter errorPresenter) {
        try {
            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                    : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    .setFlags(flags);
            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, output, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
            return intent;
        } catch (Exception ex) {
            errorPresenter.showStorageError(context);
            return null;
        }
    }

}
