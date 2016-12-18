package com.volleyverse.volleyverse;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Set;

/**
 * This class is essentially identical to the suggested implementation found in
 * the Android development guides:
 * https://developer.android.com/training/volley/requestqueue.html#singleton
 */

@SuppressLint("StaticFieldLeak")
class Singleton {

  private static final String DEFAULT_TAG = "VOLLEYVERSE";

  private static Singleton instance;
  private RequestQueue requestQueue;
  private ImageLoader imageLoader;
  private HashMap<String, Integer> tagCounter;
  private static Context context;

  private Singleton(Context ctx) {
    context = ctx;
    this.requestQueue = getRequestQueue();
    this.imageLoader =
        new ImageLoader(this.requestQueue, new LruImageCache(LruImageCache.getCacheSize(context)));
    this.tagCounter = new HashMap<>();
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
    addToRequestQueue(req, DEFAULT_TAG);
  }

  <T> void addToRequestQueue(Request<T> req, String tag) {
    if (!this.tagCounter.containsKey(tag)) {
      this.tagCounter.put(tag, 0);
    }
    this.tagCounter.put(tag, this.tagCounter.get(tag) + 1);
    req.setTag(tag);
    this.getRequestQueue().add(req);
  }

  ImageLoader getImageLoader() {
    return this.imageLoader;
  }

  void cancelRequests() {
    Set<String> keys = this.tagCounter.keySet();
    if (this.requestQueue != null) {
      for (String key : keys) {
        this.getRequestQueue().cancelAll(key);
      }
    }

    for (String key : keys) {
      this.tagCounter.put(key, 0);
    }
  }

  void cancelRequestsByTag(String tag) {
    if (this.requestQueue != null) {
      this.getRequestQueue().cancelAll(tag);
    }
    this.tagCounter.put(tag, 0);
  }
}