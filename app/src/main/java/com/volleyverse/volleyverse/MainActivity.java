package com.volleyverse.volleyverse;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity implements ArticleSelectionListener {

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

  private static final String TAG = "CONTENT";

  private DrawerLayout drawer;
  private ActionBarDrawerToggle toggle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
    this.setContentView(R.layout.activity_main);

    setupActionBar();
    setupNavigationDrawer();

    FragmentManager fragmentManager = this.getSupportFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    transaction.replace(R.id.activity_main_fragment_container, new ArticleListFragment());
    transaction.commit();
  }

  private void setupActionBar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
    if (toolbar != null) {
      setSupportActionBar(toolbar);

      this.drawer = (DrawerLayout) findViewById(R.id.activity_main_drawer);
      this.toggle = new ActionBarDrawerToggle(
          this, this.drawer, R.string.drawer_open, R.string.drawer_close);
      this.drawer.addDrawerListener(this.toggle);

      ActionBar actionBar = this.getSupportActionBar();
      if (actionBar != null) {
        actionBar.setDisplayHomeAsUpEnabled(true);
      }
    }
  }

  private void setupNavigationDrawer() {
    ListView navigation = (ListView) findViewById(R.id.activity_main_navigation);
    ArrayAdapter<String> menu =
        new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MENU_ITEMS);
    if (navigation != null) {
      navigation.setAdapter(menu);
      navigation.setOnItemClickListener(new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          drawer.closeDrawer(GravityCompat.START);
          showContent(MENU_ITEMS[position], getString(MENU_ITEMS_URL[position]));
        }
      });
    }
  }

  @Override
  public void onArticleSelected(PostListItem item) {
    this.showContent(ContentFragment.TYPE_POST, item.getUrl());
  }

  private void showContent(String type, String url) {
    this.toggle.setDrawerIndicatorEnabled(false);
    ContentFragment fragment = new ContentFragment();
    Bundle args = new Bundle();
    args.putString(ContentFragment.KEY_TYPE, type);
    args.putString(ContentFragment.KEY_URL, url);
    fragment.setArguments(args);
    FragmentManager fragmentManager = this.getSupportFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    transaction.replace(R.id.activity_main_fragment_container, fragment, TAG);
    transaction.addToBackStack(null);
    transaction.commit();
  }

  @Override
  public void onBackPressed() {
    if (this.drawer.isDrawerOpen(GravityCompat.START)) {
      this.drawer.closeDrawer(GravityCompat.START);
    } else {
      FragmentManager fragmentManager = this.getSupportFragmentManager();
      ContentFragment contentFragment =
          (ContentFragment) fragmentManager.findFragmentByTag(TAG);
      if (contentFragment != null) {
        WebView webView = contentFragment.getBody();
        if (webView.canGoBack()) {
          webView.goBack();
        } else {
          this.toggle.setDrawerIndicatorEnabled(true);
          ActionBar actionBar = this.getSupportActionBar();
          if (actionBar != null) {
            actionBar.setTitle(this.getTitle());
          }
          fragmentManager.popBackStack();
        }
      } else {
        super.onBackPressed();
      }
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
      case android.R.id.home:
        this.toggle.setDrawerIndicatorEnabled(true);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
          actionBar.setTitle(this.getTitle());
        }
        // TODO: Maybe this should replace the fragment instead?
        this.getSupportFragmentManager().popBackStack();
        return true;
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
