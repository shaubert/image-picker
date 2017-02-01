package com.shaubert.ui.imagepicker;

import java.io.File;

public interface Compression {

    boolean compressImage(File source, File destination, CompressionOptions compressionOptions);

}
