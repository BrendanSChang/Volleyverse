package com.volleyverse.volleyverse;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

  private static final int LOAD_TYPE_NOT_LOADING = 0;
  private static final int LOAD_TYPE_ON_CREATE = 1;
  private static final int LOAD_TYPE_ON_SCROLL = 2;
  private static final int LOAD_TYPE_ON_REFRESH = 3;

  // TODO: Maybe this should include feedback. Also, maybe this should be in resources.
  private static final String[] MENU_ITEMS =
      { "About Us", "Partnerships", "Jobs", "Privacy & Cookie Policy", "Terms of Use" };
  private static final int[] MENU_ITEMS_URL = {
      R.string.about_us_url,
      R.string.partnerships_url,
      R.string.jobs_url,
      R.string.privacy_and_cookie_policy_url,
      R.string.terms_of_use_url
  };

  private static final String[] EXCLUDED_POST_IDS = {"4669"};

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

  private DrawerLayout drawer;
  private ActionBarDrawerToggle toggle;
  private SwipeRefreshLayout refreshLayout;
  private ArrayList<PostListItem> listItems;
  private PostListAdapter adapter;
  private LinearLayoutManager layoutManager;
  private int loadType;
  private int page;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
    setSupportActionBar(toolbar);

    ListView navigation = (ListView) findViewById(R.id.activity_main_navigation);
    ArrayAdapter<String> menu =
        new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MENU_ITEMS);
    if (navigation != null) {
      navigation.setAdapter(menu);
      navigation.setOnItemClickListener(new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        drawer.closeDrawer(GravityCompat.START);
        Intent intent = new Intent(MainActivity.this, ContentActivity.class);
        intent.putExtra(ContentActivity.KEY_TYPE, MENU_ITEMS[page]);
        intent.putExtra(ContentActivity.KEY_URL, getString(MENU_ITEMS_URL[page]));
        startActivity(intent);
        }
      });
    }

    this.drawer = (DrawerLayout) findViewById(R.id.activity_main_drawer);
    this.toggle = new ActionBarDrawerToggle(
        this, this.drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    this.drawer.addDrawerListener(this.toggle);

    this.refreshLayout =
        (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
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

    this.listItems = new ArrayList<>();
    this.adapter = new PostListAdapter(MainActivity.this, this.listItems);
    this.layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activity_main_recycler_view);
    if (recyclerView != null) {
      recyclerView.setHasFixedSize(true);
      recyclerView.setLayoutManager(this.layoutManager);
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

    ProgressBar bar = (ProgressBar) findViewById(R.id.activity_main_progress_bar);
    if (bar != null) {
      bar.setVisibility(View.VISIBLE);
    }

    this.loadType = LOAD_TYPE_ON_CREATE;
    this.page = 1;
    loadPosts();
  }

  private void loadPosts() {
    Log.d("loadPosts", "Loading more posts, loadType: " + loadTypeToString(this.loadType));

    // Send requests.
    String options = "&page=" + page;
    // TODO: Get rid of this once it's not a problem.
    if (EXCLUDED_POST_IDS.length > 0) {
      options += "&exclude=";
      for (int i = 0; i < EXCLUDED_POST_IDS.length - 1; i++) {
        options += EXCLUDED_POST_IDS[i] + ",";
      }
      options += EXCLUDED_POST_IDS[EXCLUDED_POST_IDS.length - 1];
    }

    String restUrl = getString(R.string.rest_url) + options;
    Log.d("loadPosts", "Sending request: " + restUrl);
    JsonArrayRequest request =
        new JsonArrayRequest(
            restUrl,
            new Response.Listener<JSONArray>() {
              @Override
              public void onResponse(JSONArray response) {
                parseResponse(response);
              }
            },
            new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Response error: " + error.getMessage());
                parseResponse(null);
              }
            });
    Singleton.getInstance(this).addToRequestQueue(request);
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

  private void parseResponse(JSONArray response) {
    try {
      if (response != null) {
        Log.d("parseResponse", "Parsing response.");
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
        Log.e("parseResponse", "Response is null. Moving on.");
        this.updateAdapter(null);
      }
    } catch (JSONException e) {
      Log.e("parseResponse", "Error parsing response: " + e.getMessage());
    }
  }

  public void updateAdapter(ArrayList<PostListItem> items) {
    Log.d("updateAdapter", "Removing loading elements from layout.");
    if (this.loadType == LOAD_TYPE_ON_CREATE) {
      ProgressBar bar = (ProgressBar) findViewById(R.id.activity_main_progress_bar);
      if (bar != null) {
        bar.setVisibility(View.GONE);
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
      Log.wtf("updateAdapter", "loadType not recognized: " + this.loadType);
    }

    if (items != null) {
      Log.d("updateAdapter", "Adding new items to adapter.");
      int prevSize = this.listItems.size();
      this.listItems.addAll(items);
      this.adapter.notifyItemRangeInserted(prevSize, items.size());
      this.page++;
    } else {
      Log.d("updateAdapter", "Items is null. Not adding new items.");
    }

    this.loadType = LOAD_TYPE_NOT_LOADING;
  }

  @Override
  public void onBackPressed() {
    if (this.drawer.isDrawerOpen(GravityCompat.START)) {
      this.drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    this.toggle.syncState();
  }

  // This only seems to be for when the device changes orientation.
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    this.toggle.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = this.getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (this.toggle.onOptionsItemSelected(item)) {
      return true;
    }

    int itemId = item.getItemId();
    switch(itemId) {
      case R.id.action_settings:
      case R.id.action_feedback:
        Log.d("onOptionsItemSel", "Menu option selected. Not implemented yet.");
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    Singleton.getInstance(this).cancelRequests();
  }
}
