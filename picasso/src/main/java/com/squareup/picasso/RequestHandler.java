/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.picasso;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.NetworkInfo;

import com.squareup.picasso.Picasso.LoadedFrom;

/**
 * {@link RequestHandler} allows you to extend Picasso to load images in ways
 * that are not supported by default in the library.
 * <p>
 * <h2>Usage</h2>
 * <p>
 * {@link RequestHandler} must be subclassed to be used. You will have to
 * override two methods ({@link #canHandleRequest(Request)} and
 * {@link #load(Request)}) with your custom logic to load images.
 * </p>
 * 
 * <p>
 * You should then register your {@link RequestHandler} using
 * {@link Picasso.Builder#addRequestHandler(RequestHandler)}
 * </p>
 * 
 * <b>NOTE:</b> This is a beta feature. The API is subject to change in a
 * backwards incompatible way at any time.
 * 
 * @see Picasso.Builder#addRequestHandler(RequestHandler)
 */
public abstract class RequestHandler {
  /**
   * {@link Result} represents the result of a {@link #load(Request)} call in a
   * {@link RequestHandler}.
   * 
   * @see RequestHandler
   * @see #load(Request)
   */
  public static final class Result {
    private final Picasso.LoadedFrom loadedFrom;
    private final Bitmap bitmap;
    private final int exifOrientation;
    private final InputStream gifStream; // For GIFs only

    public Result(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
      this(bitmap, loadedFrom, 0);
    }

    /**
     * Use only when stream has been identified as containing GIF data.
     * 
     * @param gifStream {@link InputStream} containing GIF data.
     * @param loadedFrom {@link LoadedFrom}.
     */
    public Result(InputStream gifStream, Picasso.LoadedFrom loadedFrom) {
      this.gifStream = gifStream;
      this.bitmap = null;
      this.loadedFrom = loadedFrom;
      this.exifOrientation = 0;
    }

    Result(Bitmap bitmap, Picasso.LoadedFrom loadedFrom, int exifOrientation) {
      this.bitmap = bitmap;
      this.gifStream = null;
      this.loadedFrom = loadedFrom;
      this.exifOrientation = exifOrientation;
    }

    /**
     * Returns the resulting {@link Bitmap} generated from a
     * {@link #load(Request)} call.
     */
    public Bitmap getBitmap() {
      return bitmap;
    }

    /**
     * Returns the resulting {@link Picasso.LoadedFrom} generated from a
     * {@link #load(Request)} call.
     */
    public Picasso.LoadedFrom getLoadedFrom() {
      return loadedFrom;
    }

    /**
     * Returns an {@link InputStream} containing GIF data. If this is non-null,
     * it is an indication that the resource was identified as a GIF and
     * <b>not</b> decoded into a {@link Bitmap}, thus {@link #getBitmap()} will
     * return <code>null</code>. This stream can then be used by a separate GIF
     * decoding library, since Picasso does not support animated GIFs.
     */
    public InputStream getGifStream() {
      return gifStream;
    }

    /**
     * Returns the resulting EXIF orientation generated from a
     * {@link #load(Request)} call. This is only accessible to built-in
     * RequestHandlers.
     */
    int getExifOrientation() {
      return exifOrientation;
    }
  }

  /**
   * Whether or not this {@link RequestHandler} can handle a request with the
   * given {@link Request}.
   */
  public abstract boolean canHandleRequest(Request data);

  /**
   * Loads an image for the given {@link Request}.
   * 
   * @param data the {@link android.net.Uri} to load the image from.
   * @return A {@link Result} instance representing the result.
   */
  public abstract Result load(Request data) throws IOException;

  int getRetryCount() {
    return 0;
  }

  boolean shouldRetry(boolean airplaneMode, NetworkInfo info) {
    return false;
  }

  boolean supportsReplay() {
    return false;
  }

  /**
   * Lazily create {@link BitmapFactory.Options} based in given {@link Request},
   * only instantiating them if needed.
   */
  static BitmapFactory.Options createBitmapOptions(Request data) {
    final boolean justBounds = data.hasSize();
    final boolean hasConfig = data.config != null;
    BitmapFactory.Options options = null;
    if (justBounds || hasConfig) {
      options = new BitmapFactory.Options();
      options.inJustDecodeBounds = justBounds;
      if (hasConfig) {
        options.inPreferredConfig = data.config;
      }
    }
    return options;
  }

  static boolean requiresInSampleSize(BitmapFactory.Options options) {
    return options != null && options.inJustDecodeBounds;
  }

  static void calculateInSampleSize(int reqWidth, int reqHeight, BitmapFactory.Options options,
      Request request) {
    calculateInSampleSize(reqWidth, reqHeight, options.outWidth, options.outHeight, options,
        request);
  }

  static void calculateInSampleSize(int reqWidth, int reqHeight, int width, int height,
      BitmapFactory.Options options, Request request) {
    int sampleSize = 1;
    if (height > reqHeight || width > reqWidth) {
      final int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
      final int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
      sampleSize = request.centerInside ? Math.max(heightRatio, widthRatio) : Math.min(heightRatio,
          widthRatio);
    }
    options.inSampleSize = sampleSize;
    options.inJustDecodeBounds = false;
  }
}
