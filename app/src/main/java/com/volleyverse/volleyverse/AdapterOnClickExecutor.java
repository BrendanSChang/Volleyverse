package com.volleyverse.volleyverse;

/**
 * This interface should be used in cases where ViewHolder objects do not have the
 * necessary information to process click events. So instead, the ViewHolder propagates
 * the event to to the AdapterViewExecutor for further processing.
 */
interface AdapterOnClickExecutor {

  /**
   * This method should be invoked from onClick event callback in the ViewHolder.
   *
   * @param position The position of the item in the AdapterViewExecutor's list of items.
   */
  void onClick(int position);

}
