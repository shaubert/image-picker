package com.shaubert.ui.imagepicker;

import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

class Intents {

    public static Intent getPickImageIntent(Context context) {
        return new ActivityResultContracts.PickVisualMedia().createIntent(
                context,
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
    }

    public static @Nullable Uri getPickImageResult(int resultCode, @Nullable Intent data) {
        return new ActivityResultContracts.PickVisualMedia().parseResult(resultCode, data);
    }

    public static Intent takePhotoIntentOrShowError(@NonNull Uri output,
                                                    Context context,
                                                    ErrorPresenter errorPresenter) {
        try {
            Intent intent = new ActivityResultContracts.TakePicture().createIntent(context, output)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, output, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
            return intent;
        } catch (Exception ex) {
            DebugLog.logError("failed to create take photo intent", ex);
            errorPresenter.showStorageError(context);
            return null;
        }
    }

}
