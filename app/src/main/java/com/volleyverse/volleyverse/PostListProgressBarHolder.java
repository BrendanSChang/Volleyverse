package com.volleyverse.volleyverse;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

class PostListProgressBarHolder extends RecyclerView.ViewHolder {

  private ProgressBar bar;

  PostListProgressBarHolder(View view) {
    super(view);
    this.bar = (ProgressBar) view.findViewById(R.id.post_list_footer_progress_bar);
  }

  ProgressBar getBar() {
    return this.bar;
  }
}
