package com.tencent.liteav.demo.videoediter.paster.view;

import android.content.Context;
import android.view.View;

import com.tencent.liteav.demo.videoediter.R;


/**
 * Created by hanszhli on 2017/6/21.
 * <p>
 * 创建 OperationView的工厂
 */

public class TCPasterOperationViewFactory {

    public static PasterOperationView newOperationView(Context context) {
        return (PasterOperationView) View.inflate(context, R.layout.layout_paster_operation_view, null);
    }
}
