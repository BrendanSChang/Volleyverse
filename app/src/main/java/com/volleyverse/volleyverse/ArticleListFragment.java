package com.volleyverse.volleyverse;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ArticleListFragment extends Fragment {

  private static final int LOAD_TYPE_NOT_LOADING = 0;
  private static final int LOAD_TYPE_ON_CREATE = 1;
  private static final int LOAD_TYPE_ON_SCROLL = 2;
  private static final int LOAD_TYPE_ON_REFRESH = 3;

  private static final String[] EXCLUDED_POST_IDS = {"4669", "4779"};

  public static String loadTypeToString(int type) {
    switch(type) {
      case LOAD_TYPE_NOT_LOADING:
        return "NOT_LOADING";
      case LOAD_TYPE_ON_CREATE:
        return "ON_CREATE";
      case LOAD_TYPE_ON_SCROLL:
        return "ON_SCROLL";
      case LOAD_TYPE_ON_REFRESH:
        return "ON_REFRESH";
      default:
        return "Unknown";
    }
  }

  private SwipeRefreshLayout refreshLayout;
  private ArrayList<PostListItem> listItems;
  private PostListAdapter adapter;
  private int loadType;
  private int page;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.listItems = new ArrayList<>();
    this.adapter = new PostListAdapter(this.getActivity(), this.listItems);
    this.loadType = LOAD_TYPE_ON_CREATE;
    this.page = 1;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.article_list_fragment, container, false);

    this.refreshLayout =
        (SwipeRefreshLayout) rootView.findViewById(R.id.article_list_fragment_swipe_refresh_layout);
    this.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        // No need to check if loading is available because the scroll listener will
        // disable the refresh layout if it loads more items.
        page = 1;
        loadType = LOAD_TYPE_ON_REFRESH;
        loadPosts();
      }
    });
    this.refreshLayout.setColorSchemeResources(
        R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent);

    RecyclerView recyclerView =
        (RecyclerView) rootView.findViewById(R.id.article_list_fragment_recycler_view);
    if (recyclerView != null) {
      recyclerView.setHasFixedSize(true);
      final LinearLayoutManager layoutManager =
          new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false);
      recyclerView.setLayoutManager(layoutManager);
      recyclerView.setAdapter(this.adapter);
      recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
          // Only run the update if scrolled towards bottom.
          if (dy > 0 && loadType == LOAD_TYPE_NOT_LOADING) {
            int visible = layoutManager.getChildCount();
            int seen = layoutManager.findFirstVisibleItemPosition();
            int total = layoutManager.getItemCount();
            if (visible + seen >= total) {
              refreshLayout.setEnabled(false);
              // Show progress bar at bottom of list.
              listItems.add(null);
              adapter.notifyItemChanged(listItems.size() - 1);
              loadType = LOAD_TYPE_ON_SCROLL;
              loadPosts();
            }
          }
        }
      });
    }

    ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.article_list_fragment_progress_bar);
    if (bar != null) {
      bar.setVisibility(View.VISIBLE);
    }

    // This should only occur if the fragment is being created.
    if (this.listItems.size() == 0) {
      loadPosts();
    }

    return rootView;
  }

  private void loadPosts() {
    Log.d("ListFrag.loadPosts", "Loading more posts, loadType: " + loadTypeToString(this.loadType));

    // Send requests.
    String options = "&page=" + page;
    // TODO: Get rid of this once it's not a problem.
    if (EXCLUDED_POST_IDS.length > 0) {
      Log.d("length", "length: " + EXCLUDED_POST_IDS.length);
      options += "&exclude=";
      for (int i = 0; i < EXCLUDED_POST_IDS.length - 1; i++) {
        options += EXCLUDED_POST_IDS[i] + ",";
      }
      options += EXCLUDED_POST_IDS[EXCLUDED_POST_IDS.length - 1];
    }

    String restUrl = getString(R.string.rest_url) + options;
    Log.d("ListFrag.loadPosts", "Sending request: " + restUrl);
    JsonArrayRequest request =
        new JsonArrayRequest(
            restUrl,
            new Response.Listener<JSONArray>() {
              @Override
              public void onResponse(JSONArray response) {
                processResponse(response);
              }
            },
            new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Response error: " + error.getMessage());
                processResponse(null);
              }
            });
    Singleton.getInstance(this.getActivity()).addToRequestQueue(request);
  }
  private Spanned trim(Spanned content) {
    if (content == null) {
      return null;
    }

    int start = 0;
    while (Character.isWhitespace(content.charAt(start))) {
      start++;
    }

    int end = content.length() - 1;
    while (Character.isWhitespace(content.charAt(end))) {
      end--;
    }

    return (Spanned) content.subSequence(start, end + 1);
  }

  private void processResponse(JSONArray response) {
    try {
      if (response != null) {
        Log.d("ListFrag.processResp", "Processing response.");
        ArrayList<PostListItem> items = new ArrayList<>();
        for (int i = 0; i < response.length(); i++) {
          JSONObject post = response.getJSONObject(i);
          String url = post.getString("link");
          // TODO: Maybe get a different size based on the user's phone size.
          // TODO: Is this assuming something that might not always be true?
          String imageUrl =
              post.getJSONObject("_embedded")
                  .getJSONArray("wp:featuredmedia")
                  .getJSONObject(0)
                  .getString("source_url");
          String htmlTitle = post.getJSONObject("title").getString("rendered");
          String htmlExcerpt = post.getJSONObject("excerpt").getString("rendered");
          // TODO: This is deprecated in SDK version 24+. Add in a version check once the SDK version is upgraded.
          Spanned title = this.trim(Html.fromHtml(htmlTitle));
          Spanned excerpt = this.trim(Html.fromHtml(htmlExcerpt));
          items.add(new PostListItem(url, imageUrl, title, excerpt));
        }
        this.updateAdapter(items);
      } else {
        Log.e("ListFrag.processResp", "Response is null. Moving on.");
        this.updateAdapter(null);
      }
    } catch (JSONException e) {
      Log.e("ListFrag.processResp", "Error parsing response: " + e.getMessage());
    }
  }

  public void updateAdapter(ArrayList<PostListItem> items) {
    Log.d("ListFrag.updateAdapter", "Removing loading elements from layout.");
    if (this.loadType == LOAD_TYPE_ON_CREATE) {
      View rootView = this.getView();
      if (rootView != null) {
        ProgressBar bar =
            (ProgressBar) rootView.findViewById(R.id.article_list_fragment_progress_bar);
        if (bar != null) {
          bar.setVisibility(View.GONE);
        }
      }
    } else if (this.loadType == LOAD_TYPE_ON_SCROLL) {
      this.listItems.remove(this.listItems.size() - 1);
      this.adapter.notifyItemRemoved(this.listItems.size());
      this.refreshLayout.setEnabled(true);
    } else if (this.loadType == LOAD_TYPE_ON_REFRESH) {
      // TODO: Update list and add/remove items as needed instead of removing then adding.
      int numItems = this.listItems.size();
      this.listItems.clear();
      this.adapter.notifyItemRangeRemoved(0, numItems);
      if (this.refreshLayout.isRefreshing()) {
        this.refreshLayout.setRefreshing(false);
      }
    } else {
      Log.wtf("ListFrag.updateAdapter", "loadType not recognized: " + this.loadType);
    }

    if (items != null) {
      Log.d("ListFrag.updateAdapter", "Adding new items to adapter.");
      int prevSize = this.listItems.size();
      this.listItems.addAll(items);
      this.adapter.notifyItemRangeInserted(prevSize, items.size());
      this.page++;
    } else {
      Log.d("ListFrag.updateAdapter", "Items is null. Not adding new items.");
    }

    this.loadType = LOAD_TYPE_NOT_LOADING;
  }
}
