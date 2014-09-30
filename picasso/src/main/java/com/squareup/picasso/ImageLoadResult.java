package com.squareup.picasso;

import java.io.InputStream;

import android.graphics.Bitmap;

public class ImageLoadResult {

  public Bitmap bitmap;
  public InputStream gifStream;

  ImageLoadResult() {
    this.bitmap = null;
    this.gifStream = null;
  }

  ImageLoadResult(Bitmap bitmap) {
    this.bitmap = bitmap;
    this.gifStream = null;
  }

}
