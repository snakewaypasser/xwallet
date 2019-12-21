/**
 * Copyright (c) 2019 by snakeway
 * <p>
 * All rights reserved.
 */
package com.my.xwallet;


import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.my.adapters.recyclerviewadapter.LanguageSettingActivity_RecyclerViewAdapter;
import com.my.base.recyclerviewlibrary.models.ViewItem;
import com.my.base.recyclerviewlibrary.views.BaseRecyclerViewFromFrameLayout;
import com.my.models.local.Setting;
import com.my.utils.LanguageTool;
import com.my.xwallet.aidl.WalletOperateManager;

import java.util.ArrayList;
import java.util.List;

public class LanguageSettingActivity extends NewBaseActivity {
    private ImageView imageViewBack;
    private TextView textViewTitle;
    private BaseRecyclerViewFromFrameLayout baseRecyclerViewFromFrameLayout;

    private View.OnClickListener onClickListener;
    private LanguageSettingActivity_RecyclerViewAdapter languageSettingActivity_RecyclerViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_setting);
        initAll();
    }


    @Override
    protected void initHandler() {

    }

    @Override
    protected void initUi() {
        imageViewBack = (ImageView) findViewById(R.id.imageViewBack);
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        baseRecyclerViewFromFrameLayout = (BaseRecyclerViewFromFrameLayout) findViewById(R.id.baseRecyclerViewFromFrameLayout);
        initBaseRecyclerViewFromFrameLayout();
        onClickListener();
    }

    @Override
    protected void initConfigUi() {
        textViewTitle.setText(R.string.activity_language_setting_textViewTitle_text);
    }

    @Override
    protected void initHttp() {

    }

    @Override
    protected void initOther() {
        loadLanguages();
    }

    private void initBaseRecyclerViewFromFrameLayout() {
        baseRecyclerViewFromFrameLayout.getSwipeRefreshLayout().setEnabled(false);
    }

    private void onClickListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.imageViewBack:
                        doBack();
                        break;
                    default:
                        break;
                }
            }
        };
        imageViewBack.setOnClickListener(onClickListener);
    }

    private void initOrRefreshAdapter(List<ViewItem> viewItems, int selectPosition) {
        if (viewItems == null) {
            viewItems = new ArrayList<ViewItem>();
        }
        if (languageSettingActivity_RecyclerViewAdapter == null) {
            languageSettingActivity_RecyclerViewAdapter = new LanguageSettingActivity_RecyclerViewAdapter(LanguageSettingActivity.this, baseRecyclerViewFromFrameLayout.getRecyclerView(), viewItems);
            languageSettingActivity_RecyclerViewAdapter.setSelectPosition(selectPosition);
            languageSettingActivity_RecyclerViewAdapter.setOnLanguageSettingListener(new LanguageSettingActivity_RecyclerViewAdapter.OnLanguageSettingListener() {
                @Override
                public void onItemSelect(String language) {
                    updateLanguageSetting(language);
                }

            });
            baseRecyclerViewFromFrameLayout.setAdapter(languageSettingActivity_RecyclerViewAdapter);
        } else {
            languageSettingActivity_RecyclerViewAdapter.setSelectPosition(selectPosition);
            TheApplication.replaceAllFormBaseRecyclerViewAdapter(languageSettingActivity_RecyclerViewAdapter, viewItems, baseRecyclerViewFromFrameLayout.getRecyclerView());
        }
    }

    private void loadLanguages() {
        Object[] languagesInfo = LanguageTool.getLanguagesInfo(LanguageSettingActivity.this);
        List<String> languages = (List<String>) languagesInfo[0];
        int selectPosition = (int) languagesInfo[1];

        List<ViewItem> viewItems = new ArrayList<ViewItem>();
        for (int i = 0; i < languages.size(); i++) {
            ViewItem viewItem = new ViewItem(ViewItem.VIEW_TYPE_NORMAL_ITEM_TYPE1, languages.get(i));
            viewItems.add(viewItem);
        }
        initOrRefreshAdapter(viewItems, selectPosition);
    }


    private void updateLanguageSetting(final String language) {
        Setting setting = TheApplication.getSetting();
        setting.setLanguage(language);
        TheApplication.setAndWriteSetting(setting);
        changeWalletServiceLanguage(language);
        Intent intent = new Intent(LanguageSettingActivity.this,
                MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void changeWalletServiceLanguage(String language) {
        WalletOperateManager walletOperateManager = TheApplication.getTheApplication().getWalletServiceHelper().getWalletOperateManager();
        if (walletOperateManager == null) {
            return;
        }
        try {
            walletOperateManager.changeLanguage(language);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doBack() {
        super.doBack();
        finish();
    }

}