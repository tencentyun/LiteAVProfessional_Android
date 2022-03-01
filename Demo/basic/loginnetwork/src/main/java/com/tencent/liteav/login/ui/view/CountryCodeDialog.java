package com.tencent.liteav.login.ui.view;

import android.content.Context;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.tencent.liteav.login.R;
import com.tencent.liteav.login.model.CountryCodeInfo;

import java.util.List;
import java.util.Locale;

public class CountryCodeDialog extends BottomSheetDialog {
    private RecyclerView mRvCountryCode;

    public CountryCodeDialog(Context context, List<CountryCodeInfo.CountryCodeEntity> list, final OnItemClickListener listener) {
        super(context, R.style.LoginBottomDialog);
        setContentView(R.layout.login_view_country_code_dialog);
        mRvCountryCode = findViewById(R.id.rv_country_code);
        Locale locale = context.getResources().getConfiguration().locale;
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        mRvCountryCode.setLayoutManager(linearLayoutManager);
        CountryCodeListAdapter adapter = new CountryCodeListAdapter(context, locale.getLanguage(), list, new CountryCodeListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CountryCodeInfo.CountryCodeEntity countryCodeEntity) {
                listener.onItemClick(countryCodeEntity);
                dismiss();
            }
        });
        mRvCountryCode.setAdapter(adapter);
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }


    public interface OnItemClickListener {
        void onItemClick(CountryCodeInfo.CountryCodeEntity countryCodeEntity);
    }
}