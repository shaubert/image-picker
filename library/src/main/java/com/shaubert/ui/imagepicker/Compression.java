package com.shaubert.ui.imagepicker;

import android.content.Context;
import android.net.Uri;

public interface Compression {

    boolean compressImage(Context context, Uri source, Uri destination, CompressionOptions compressionOptions);

}
