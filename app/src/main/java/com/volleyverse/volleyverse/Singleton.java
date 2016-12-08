package com.volleyverse.volleyverse;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * This class is essentially identical to the suggested implementation found in
 * the Android development guides:
 * https://developer.android.com/training/volley/requestqueue.html#singleton
 */

@SuppressLint("StaticFieldLeak")
class Singleton {

  private static final String TAG = "VOLLEYVERSE";

  private static Singleton instance;
  private RequestQueue requestQueue;
  private ImageLoader imageLoader;
  private static Context context;

  private Singleton(Context ctx) {
    context = ctx;
    this.requestQueue = getRequestQueue();
    this.imageLoader =
        new ImageLoader(this.requestQueue, new LruImageCache(LruImageCache.getCacheSize(context)));
  }

  static synchronized Singleton getInstance(Context context) {
    if (instance == null) {
      instance = new Singleton(context);
    }
    return instance;
  }

  private RequestQueue getRequestQueue() {
    if (this.requestQueue == null) {
      // getApplicationContext() is key, it keeps you from leaking the
      // Activity or BroadcastReceiver if someone passes one in.
      this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }
    return this.requestQueue;
  }

  <T> void addToRequestQueue(Request<T> req) {
    req.setTag(TAG);
    this.getRequestQueue().add(req);
  }

  ImageLoader getImageLoader() {
    return this.imageLoader;
  }

  void cancelRequests() {
    if (this.requestQueue != null) {
      this.getRequestQueue().cancelAll(TAG);
    }
  }
}