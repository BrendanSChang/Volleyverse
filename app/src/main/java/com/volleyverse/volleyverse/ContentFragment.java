package com.volleyverse.volleyverse;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

// Javascript is used to remove certain tags from the loaded HTML.
//@SuppressWarnings({"SetJavascriptEnabled", "AddJavascriptInterface"})
@SuppressWarnings("SetJavascriptEnabled")
public class ContentFragment extends Fragment {

  static final String KEY_URL = "URL";
  static final String KEY_TYPE = "TYPE";
  static final String TYPE_POST = "POST";

  private static final int PROGRESS_SCALE = 100;
  // Each row in these arrays is a tuple containing an element to remove from the DOM.
  // The first element is the matching pattern for the opening tag and the second element
  // is the base tag of the element.
  private static final String[][] POST_ELEMENTS_REMOVE = {
      {"<nav", "nav"},
      {"<div class=\"top-ads hidden-lg\"", "div"},
      {"<div class=\"amp-wp-categories\"", "div"}
  };
  private static final String[][] NON_POST_ELEMENTS_REMOVE = {
      {"<header", "header"},
      {"<div class=\"custom-loaders\"", "div"},
      {"<div class=\"vw-page-title-box clearfix\"", "div"}
  };

  private static final String TAG = "VOLLEYVERSE_CONTENT";

  private String url;
  private String type;
  private boolean isPost;
  private WebView body;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // TODO: Add comments
    Bundle args = this.getArguments();
    this.url = args.getString(KEY_URL);
    this.type = args.getString(KEY_TYPE);
    if (type != null) {
      this.isPost = type.equals(TYPE_POST);
    }
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final MainActivity activity = (MainActivity) this.getActivity();
    ActionBar actionBar = activity.getSupportActionBar();
    if (actionBar != null) {
      if (!this.isPost) {
        actionBar.setTitle(this.type);
      }
    }

    View rootView = inflater.inflate(R.layout.content_fragment, container, false);

    this.body = (WebView) rootView.findViewById(R.id.content_fragment_body);
    WebSettings settings = this.body.getSettings();
    settings.setAppCacheEnabled(true);
    settings.setAppCachePath(activity.getApplicationContext().getCacheDir().getAbsolutePath());
    settings.setJavaScriptEnabled(true);

    this.body.setWebChromeClient(new WebChromeClient() {
      @Override
      public void onProgressChanged(WebView view, int progress) {
        activity.setProgress(progress * PROGRESS_SCALE);
      }
    });
    this.body.setWebViewClient(new ContentFragment.ArticleWebViewClient());

    // Hide the WebView for now to show the ProgressBar.
    this.body.setVisibility(View.GONE);
    ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.content_fragment_progress_bar);
    if (bar != null) {
      bar.setVisibility(View.VISIBLE);
    }

    this.loadBody();

    return rootView;
  }

  private void loadBody() {
    final MainActivity activity = (MainActivity) this.getActivity();
    Log.d("ContentFrag.loadBody", "Sending request: " + this.url);
    StringRequest request =
        new StringRequest(
            Request.Method.GET,
            this.url,
            new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                processResponse(response);
              }
            }, new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            VolleyLog.e("Response error: " + error.getMessage());
          }
        });
    Singleton.getInstance(activity).addToRequestQueue(request, TAG);
  }

  private void processResponse(String response) {
    Log.d("ContentFrag.processResp", "Processing response.");

    StringBuilder result = new StringBuilder(response);
    String[][] elementsToRemove = this.isPost ? POST_ELEMENTS_REMOVE : NON_POST_ELEMENTS_REMOVE;
    for (String[] element : elementsToRemove) {
      String open = "<" + element[1];
      String close = "</" + element[1] + ">";

      // Find the start and end positions of the element. The position of the closing tag will
      // be incorrect if there are nested elements of the same tag.
      int start = result.indexOf(element[0]);
      // This will be incorrect if there are nested elements of the same tag.
      int end = result.indexOf(close, start);

      // Find the total number of nested elements of the same tag.
      int count = 0;
      int cur = result.indexOf(open, start + open.length());
      while (cur != -1 && cur < end) {
        count++;
        cur = result.indexOf(open, cur + open.length());
      }

      // Find the matching closing tag.
      for (int i = 0; i < count; i++) {
        end = result.indexOf(close, end + close.length());
      }

      // Finally, remove the entire child.
      result.delete(start, end + close.length());
    }

    Log.d("ContentFrag.processResp", "Load cleaned data into body.");
    this.body.loadDataWithBaseURL(this.url, result.toString(), "text/html", "utf-8", "");
  }

  @Override
  public void onResume() {
    this.body.onResume();
    super.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    this.body.onPause();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    final MainActivity activity = (MainActivity) this.getActivity();
    Singleton.getInstance(activity).cancelRequestsByTag(TAG);
  }

  @Override
  public void onDestroy() {
    if (this.body != null) {
      this.body.destroy();
    }
    super.onDestroy();
  }

  WebView getBody() {
    return this.body;
  }

  private class ArticleWebViewClient extends WebViewClient {
    @Override
    public void onPageFinished(WebView view, String url) {
      View rootView = getView();
      if (rootView != null) {
        ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.content_fragment_progress_bar);
        if (bar != null) {
          bar.setVisibility(View.GONE);
        }
      }
      body.setVisibility(View.VISIBLE);
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
      View rootView = getView();
      if (rootView != null) {
        ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.content_fragment_progress_bar);
        if (bar != null) {
          bar.setVisibility(View.GONE);
        }
      }
      // TODO: Do something better here.
      Toast.makeText(view.getContext(), "Failed to load", Toast.LENGTH_SHORT).show();
    }
  }
}
