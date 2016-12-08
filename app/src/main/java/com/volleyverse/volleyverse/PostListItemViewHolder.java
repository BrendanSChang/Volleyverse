package com.volleyverse.volleyverse;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

class PostListItemViewHolder
    extends RecyclerView.ViewHolder implements View.OnClickListener {

  private final static int POSITION_NOT_SET = -1;

  private NetworkImageView image;
  private TextView title;
  private TextView excerpt;
  private AdapterViewExecutor executor;
  private int position;

  PostListItemViewHolder(View view, AdapterViewExecutor executor) {
    super(view);
    view.setOnClickListener(this);
    this.image = (NetworkImageView) view.findViewById(R.id.post_list_item_image);
    this.title = (TextView) view.findViewById(R.id.post_list_item_title);
    this.excerpt = (TextView) view.findViewById(R.id.post_list_item_excerpt);
    this.executor = executor;
    this.position = POSITION_NOT_SET;
  }

  NetworkImageView getImage() {
    return this.image;
  }

  TextView getTitle() {
    return this.title;
  }

  TextView getExcerpt() {
    return this.excerpt;
  }

  void setPosition(int position) {
    this.position = position;
  }

  @Override
  public void onClick(View v) {
    if (this.position != POSITION_NOT_SET) {
      executor.executeView(this.position);
    }
  }
}
