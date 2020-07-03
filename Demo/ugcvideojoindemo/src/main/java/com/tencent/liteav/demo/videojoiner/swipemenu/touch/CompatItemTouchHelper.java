package com.tencent.liteav.demo.videojoiner.swipemenu.touch;

import android.support.v7.widget.helper.ItemTouchHelper;

public class CompatItemTouchHelper extends ItemTouchHelper {

    private Callback mTouchCallback;

    public CompatItemTouchHelper(Callback callback) {
        super(callback);
        mTouchCallback = callback;
    }

    /**
     * Developer callback which controls the behavior of ItemTouchHelper.
     *
     * @return {@link Callback}
     */
    public Callback getCallback() {
        return mTouchCallback;
    }
}
