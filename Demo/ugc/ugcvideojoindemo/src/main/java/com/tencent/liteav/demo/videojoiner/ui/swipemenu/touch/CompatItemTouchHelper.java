package com.tencent.liteav.demo.videojoiner.ui.swipemenu.touch;

import androidx.recyclerview.widget.ItemTouchHelper;

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
