package com.volleyverse.volleyverse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

class PostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements AdapterViewExecutor {

  private static final int ITEM_CARD = 0;
  private static final int ITEM_PROGRESS = 1;

  private final Context context;
  private List<PostListItem> items;

  PostListAdapter(Activity context, ArrayList<PostListItem> items) {
    this.context = context;
    this.items = items;
  }

  @Override
  public int getItemViewType(int position) {
    if (this.items.get(position) != null) {
      return ITEM_CARD;
    } else {
      return ITEM_PROGRESS;
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    RecyclerView.ViewHolder holder;
    if (viewType == ITEM_CARD) {
      View view =
          LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_item, parent, false);
      holder = new PostListItemViewHolder(view, this);
    } else {
      View view =
          LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_footer, parent, false);
      holder = new PostListProgressBarHolder(view);
    }
    return holder;
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (holder instanceof PostListItemViewHolder) {
      PostListItem item = this.items.get(position);
      ((PostListItemViewHolder) holder).getImage().setImageUrl(
          item.getImageUrl(),
          Singleton.getInstance(this.context).getImageLoader());
      ((PostListItemViewHolder) holder).getTitle().setText(item.getTitle());
      ((PostListItemViewHolder) holder).getExcerpt().setText(item.getExcerpt());
      ((PostListItemViewHolder) holder).setPosition(position);
    } else {
      ((PostListProgressBarHolder) holder).getBar().setIndeterminate(true);
    }
  }

  @Override
  public int getItemCount() {
    return this.items.size();
  }

  @Override
  public void executeView(int position) {
    PostListItem item = this.items.get(position);
    Intent intent = new Intent(this.context, ContentActivity.class);
    intent.putExtra(ContentActivity.KEY_TYPE, ContentActivity.TYPE_POST);
    intent.putExtra(ContentActivity.KEY_URL, item.getUrl());
    this.context.startActivity(intent);
  }
}
