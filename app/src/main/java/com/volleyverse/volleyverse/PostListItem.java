package com.volleyverse.volleyverse;

import android.text.Spanned;

class PostListItem {
  private String url;
  private String imageUrl;
  private Spanned title;
  private Spanned excerpt;

  PostListItem(String url, String imageUrl, Spanned title, Spanned excerpt) {
    this.url = url;
    this.imageUrl = imageUrl;
    this.title = title;
    this.excerpt = excerpt;
  }

  String getUrl() {
    return this.url;
  }

  String getImageUrl() {
    return this.imageUrl;
  }

  Spanned getTitle() {
    return this.title;
  }

  Spanned getExcerpt() {
    return this.excerpt;
  }
}
