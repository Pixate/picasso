/*
 * Copyright (C) 2013 Square, Inc.
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

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;
import java.io.InputStream;

import com.squareup.picasso.Picasso.LoadedFrom;

import static android.content.ContentResolver.SCHEME_CONTENT;
import static com.squareup.picasso.Picasso.LoadedFrom.DISK;

class ContentStreamRequestHandler extends RequestHandler {
  final Context context;

  ContentStreamRequestHandler(Context context) {
    this.context = context;
  }

  @Override public boolean canHandleRequest(Request data) {
    return SCHEME_CONTENT.equals(data.uri.getScheme());
  }

  @Override public Result load(Request data) throws IOException {
    return decodeContentStream(data, DISK, /* exifOrientation */ 0);
  }

  protected Result decodeContentStream(Request data, LoadedFrom loadedFrom, int exifOrientation)
      throws IOException {
    ContentResolver contentResolver = context.getContentResolver();
    final BitmapFactory.Options options = createBitmapOptions(data);
    if (requiresInSampleSize(options)) {
      InputStream is = null;
      try {
        is = contentResolver.openInputStream(data.uri);
        GifPrecheckResult precheck = precheckForGif(is);
        if (precheck.isGif) {
          return new Result(precheck.inputStream, loadedFrom);
        }
        is = precheck.inputStream;
        BitmapFactory.decodeStream(is, null, options);
      } finally {
        Utils.closeQuietly(is);
      }
      calculateInSampleSize(data.targetWidth, data.targetHeight, options, data);
    }
    InputStream is = contentResolver.openInputStream(data.uri);
    GifPrecheckResult precheck = precheckForGif(is);
    if (precheck.isGif) {
      return new Result(precheck.inputStream, loadedFrom);
    }
    is = precheck.inputStream;
    try {
      Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
      return new Result(bitmap, loadedFrom, exifOrientation);
    } finally {
      Utils.closeQuietly(is);
    }
  }
}
