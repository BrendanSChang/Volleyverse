package com.volleyverse.volleyverse;

/**
 * This interface should be used in cases where click events on a ViewHolder should
 * start a new Activity, but the ViewHolder does not have all of the necessary
 * information to add to the Intent. Instead, the ViewHolder propagates the event to
 * to the AdapterViewExecutor for further processing.
 */
interface AdapterViewExecutor {

  /**
   * This method should be invoked from onClick in the ViewHolder. The AdapterViewExecutor
   * should start the new Activity here.
   *
   * @param position The position of the item in the AdapterViewExecutor's list of items.
   */
  void executeView(int position);

}
