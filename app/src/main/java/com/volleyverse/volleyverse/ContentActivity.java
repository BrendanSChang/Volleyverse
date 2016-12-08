package com.volleyverse.volleyverse;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

// Javascript is used to remove certain tags from the loaded HTML.
@SuppressWarnings({"SetJavascriptEnabled", "AddJavascriptInterface"})
public class ContentActivity extends AppCompatActivity {

  public static final String KEY_TYPE = "TYPE";
  public static final String KEY_URL = "URL";

  public static final String TYPE_POST = "POST";

  private static final int PROGRESS_SCALE = 100;

  private boolean isPost;
  private WebView body;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
    setContentView(R.layout.activity_content);

    // TODO: Add author, publish date, and comments
    Intent i = this.getIntent();
    String type = i.getStringExtra(KEY_TYPE);
    String url = i.getStringExtra(KEY_URL);

    this.isPost = type.equals(TYPE_POST);

    Toolbar toolbar = (Toolbar) findViewById(R.id.activity_content_toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      if (!this.isPost) {
        actionBar.setTitle(type);
      }
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    this.body = (WebView) findViewById(R.id.activity_content_body);
    if (this.body != null) {
      WebSettings settings = this.body.getSettings();
      settings.setAppCacheEnabled(true);
      settings.setAppCachePath(this.getApplicationContext().getCacheDir().getAbsolutePath());
      settings.setJavaScriptEnabled(true);
      this.body.setWebChromeClient(new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int progress) {
          setProgress(progress * PROGRESS_SCALE);
        }
      });
      this.body.setWebViewClient(new ArticleWebViewClient());
      this.body.loadUrl(url);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if ((keyCode == KeyEvent.KEYCODE_BACK) && this.body.canGoBack()) {
      this.body.goBack();
      return true;
    } else if ((keyCode == KeyEvent.KEYCODE_FORWARD) && this.body.canGoForward()) {
      this.body.goForward();
      return true;
    }

    return super.onKeyDown(keyCode, event);
  }

  private class ArticleWebViewClient extends WebViewClient {
    // TODO: This works okay but the javascript removing elements is visible to the user.
    /**
     * This method removes certain elements from the loaded HTML to provide a cleaner
     * look to the user. These include navigation bars, ads, and superfluous headers.
     *
     * @param view The WebView with the loaded content.
     */
    @Override
    public void onPageFinished(WebView view, String url) {
      // The indentation pattern is broken here to align the javascript.
      StringBuilder javascript = new StringBuilder(
      "javascript:{");
      if (isPost) {
        javascript.append(
          "var nav = document.getElementsByTagName('nav')[0];" +
          "if (nav) {" +
              "nav.parentNode.removeChild(nav);" +
          "}" +
          "var topAd = document.getElementsByClassName('top-ads hidden-lg')[0];" +
          "if (topAd) {" +
              "topAd.parentNode.removeChild(topAd);" +
          "}" +
          "var breadcrumbs = document.getElementsByClassName('amp-wp-categories')[0];" +
          "if (breadcrumbs) {" +
              "breadcrumbs.parentNode.removeChild(breadcrumbs);" +
          "}" +
          "var layout = document.getElementsByClassName('amp-layout')[0];" +
          "if (layout) {" +
              "layout.style.marginTop = 0;" +
          "}"
        );
      } else {
        javascript.append(
          "var header = document.getElementsByTagName('header')[0];" +
          "if (header) {" +
              "header.parentNode.removeChild(header);" +
          "}" +
          "var nav = document.getElementsByClassName('custom-loaders')[0];" +
          "if (nav) {" +
              "nav.parentNode.removeChild(nav);" +
          "}" +
          "var title = document.getElementsByClassName('vw-page-title-box clearfix')[0];" +
          "if (title) {" +
              "title.parentNode.removeChild(title);" +
          "}");
      }
      javascript.append(
      "}");
      view.loadUrl(javascript.toString());
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      String host = Uri.parse(url).getHost();
      if (host.equals(getString(R.string.volleyverse_host))) {
        return false;
      }

      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      startActivity(intent);
      return true;
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
      // TODO: Do something better here.
      Toast.makeText(getApplicationContext(), "Failed to load", Toast.LENGTH_SHORT).show();
    }
  }
}
