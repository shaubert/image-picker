# ImagePicker

Preview images, take photos from camera, pick images from documents, and crop them.

## Gradle
    
    repositories {
        maven{url "https://github.com/shaubert/maven-repo/raw/master/releases"}
    }
    dependencies {
        compile 'com.shaubert.ui.imagepicker:library:0.2.9'
    }

## References
  *  [Cropper](https://github.com/edmodo/cropper)
  *  [PhotoView](https://github.com/chrisbanes/PhotoView)

## How-to

Setup `ImagePickerConfig`:
    
    ImagePickerConfig.setup()
        .imageLoader() //You can use GlideImageLoader or implement simple interface to load images with other library
        .openImageActivityClass() //[Optional] Activity class to show fullscreen image
        .cropImageActivityClass() //[Optional] Activity class to crop image
        .apply();
            
To pick/crop image:

    ImagePicker imagePicker = new ImagePicker(
                ImagePickerController.builder()
                        .activity(this)
                        .fragment(this) //or fragment
                        .editActionsPresenter(new EditActionsDialogPresenter(this, getSupportFragmentManager(), "picker-dialog"))
                        .privatePhotos(true) //to hide taken photos
                        .compression(new DefaultCompression()) //optional
                        .errorPresenter(new ToastErrorPresenter()) //optional
                        .tag("picker-controller")
                        .build(),
                "picker");
    imagePicker.setDefaultImageUrl("http://sipi.usc.edu/database/preview/misc/4.2.05.png", true);
    
    imagePicker.setupViews(remoteImageView, //required
          pickButton, //optional
          loadingProgress, //optional
          errorView); //optional
          
    //optional image comperession
    imagePicker.setCompressionCallback(new ImagePickerController.CompressionCallback() {
            @Nullable
            @Override
            public CompressionOptions getCompressionOptions(@NonNull Uri imageUri) {
                return CompressionOptions.newBuilder()
                        .maxFileSize(1024 * 200)
                        .targetHeight(512)
                        .targetWidth(512)
                        .build();
            }
    });
                    
    //optional callback to enable cropping
    imagePicker.setCropCallback(new ImagePickerController.CropCallback() {
            @Override
            @Nullable
            CropOptions.Builder getCropOptions(@NonNull Uri imageUri);
                return CropOptions.newBuilder()
                        .minHeight(200) //if image frame smaller error message will be shown
                        .minWidth(200) //if image frame smaller error message will be shown
                        .maxHeight(400) //if image frame bigger image will be downscaled
                        .maxWidth(400) //if image frame bigger image will be downscaled
                        .aspectX(1) //image frame aspect ratio
                        .aspectY(1); //image frame aspect ratio
            }
    });
        
    //you need to attach it to `LifecycleDelegate` or manualy call all `distach*` methods of `LifecycleDispatcher`.
    attachToLifecycle(imagePicker);
