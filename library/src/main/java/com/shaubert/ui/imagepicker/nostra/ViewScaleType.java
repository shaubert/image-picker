package com.shaubert.ui.imagepicker.nostra;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public enum ViewScaleType {
	/**
	 * Scale the image uniformly (maintain the image's aspect ratio) so that at least one dimension (width or height) of
	 * the image will be equal to or less the corresponding dimension of the view.
	 */
	FIT_INSIDE,
	/**
	 * Scale the image uniformly (maintain the image's aspect ratio) so that both dimensions (width and height) of the
	 * image will be equal to or larger than the corresponding dimension of the view.
	 */
	CROP;
}