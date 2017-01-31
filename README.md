# ImagePicker

Preview images, take photos from camera, pick images from documents, and crop them.

## Gradle
    
    repositories {
        maven{url "https://github.com/shaubert/maven-repo/raw/master/releases"}
    }
    dependencies {
        compile 'com.shaubert.ui.imagepicker:library:0.1'
    }

## References
  *  [Cropper](https://github.com/edmodo/cropper)
  *  [PhotoView](https://github.com/chrisbanes/PhotoView)

## How-to

Setup `ImagePicker`:
    
    new ImagePicker.Setup()
        .imageLoader()
        .openImageActivityClass() //[Optional] Activity class to show fullscreen image
        .cropImageActivityClass() //[Optional] Activity class to show fullscreen image
        .apply();
            
To pick/crop image:

    ImagePicker imagePicker = new ImagePicker(this, "picker");    
    imagePicker.setDefaultImageUrl("http://sipi.usc.edu/database/preview/misc/4.2.05.png", true);
    
    imagePicker.setupViews(remoteImageView, //required
          pickButton, //optional
          loadingProgress, //optional
          errorView); //optional
          
    //optional comperssion of picked image
    imagePicker.setCompressionOptions(CompressionOptions.newBuilder()
                    .maxFileSize(1024 * 200)
                    .targetHeight(512)
                    .targetWidth(512)
                    .targetScaleType(ViewScaleType.CROP)
                    .build());
                    
    //optional callback to enable cropping
    imagePicker.setCropCallback(new ImagePickerController.CropCallback() {
        @Override
        public boolean shouldCrop(@NonNull File imageFile) {
            return true;
        }

        @Override
        public void setupCropOptions(@NonNull File imageFile, @NonNull CropOptions.Builder builder) {
            builder.minHeight(200) //if image frame smaller error message will be shown
                    .minWidth(200) //if image frame smaller error message will be shown
                    .maxHeight(400) //if image frame bigger image will be downscaled
                    .maxWidth(400) //if image frame bigger image will be downscaled
                    .aspectX(1) //image frame aspect ratio
                    .aspectY(1); //image frame aspect ratio
        }
    });
    
    //to hide taken photos from gallery 
    imagePicker.setPrivatePhotos(true);
    
    //you need to attach it to `LifecycleDelegate` or manualy call all `distach*` methods of `LifecycleDispatcher`.
    attachToLifecycle(imagePicker);