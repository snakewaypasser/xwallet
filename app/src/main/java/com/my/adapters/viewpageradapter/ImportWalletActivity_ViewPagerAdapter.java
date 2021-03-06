/**
 * Copyright (c) 2019 by snakeway
 * <p>
 * All rights reserved.
 */
package com.my.adapters.viewpageradapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.my.xwallet.fragment.BaseFragment;
import com.my.xwallet.fragment.ImportWalletActivity_Fragment_Keys;
import com.my.xwallet.fragment.ImportWalletActivity_Fragment_Mnemonic;
import com.my.xwallet.uihelp.ActivityHelp;

public class ImportWalletActivity_ViewPagerAdapter extends FragmentStatePagerAdapter {

    private String set_wallet_name;
    private String set_wallet_password;
    private String set_wallet_description;
    private String[] titles;

    public ImportWalletActivity_ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, String set_wallet_name, String set_wallet_password, String set_wallet_description, String[] titles) {
        super(fm, behavior);
        if (titles == null) {
            throw new NullPointerException("titles为null");
        }
        this.set_wallet_name = set_wallet_name;
        this.set_wallet_password = set_wallet_password;
        this.set_wallet_description = set_wallet_description;
        this.titles = titles;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt(BaseFragment.POSITION_KEY, position);
        bundle.putString(ActivityHelp.SET_WALLET_NAME_KEY, set_wallet_name);
        bundle.putString(ActivityHelp.SET_WALLET_PASSWORD_KEY, set_wallet_password);
        bundle.putString(ActivityHelp.SET_WALLET_DESCRIPTION_KEY, set_wallet_description);
        Fragment fragment = null;
        if (position == 0) {
            fragment = new ImportWalletActivity_Fragment_Mnemonic();
            fragment.setArguments(bundle);
        } else if (position == 1) {
            fragment = new ImportWalletActivity_Fragment_Keys();
            fragment.setArguments(bundle);
        } else {
            fragment = new BaseFragment();
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

}