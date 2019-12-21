/**
 * Copyright (c) 2019 by snakeway
 * <p>
 * All rights reserved.
 */
package com.my.xwallet;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.my.base.BaseActivity;
import com.my.utils.ClipboardTool;
import com.my.utils.DaoTool;
import com.my.utils.WalletServiceHelper;
import com.my.utils.database.AppDatabase;
import com.my.utils.database.entity.TransactionInfo;
import com.my.utils.database.entity.Wallet;
import com.my.xwallet.aidl.OnNormalListener;
import com.my.xwallet.aidl.WalletOperateManager;
import com.my.xwallet.aidl.manager.XManager;
import com.my.xwallet.uihelp.ActivityHelp;
import com.my.xwallet.uihelp.PopupWindowHelp;
import com.my.xwallet.uihelp.ProgressDialogHelp;

import java.util.List;

public class WalletDetailsActivity extends NewBaseActivity {
    public static final int TYPE_SHOW_ADDRES = 1;
    public static final int TYPE_SHOW_MNEMONIC_WORDS = 2;
    public static final int TYPE_SHOW_PRIVATE_KEYS = 3;

    private Wallet wallet;

    private ImageView imageViewBack;
    private TextView textViewTitle;
    private TextView textViewAssetsTitle;
    private TextView textViewAccount;
    private TextView textViewWalletNameContent;
    private TextView textViewWalletAddressContent;
    private RelativeLayout relativeLayoutWalletAddress;
    private RelativeLayout relativeLayoutPasswordHint;
    private RelativeLayout relativeLayoutExportMnemonicWords;
    private RelativeLayout relativeLayoutExportPrivateKeys;
    private RelativeLayout relativeLayoutDeleteWallet;
    private View.OnClickListener onClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_details);
        Intent intent = getIntent();
        wallet = (Wallet) intent.getSerializableExtra(ActivityHelp.WALLET_KEY);
        initAll();
    }

    @Override
    protected void initHandler() {
        handler = new Handler();
    }

    @Override
    protected void initUi() {
        imageViewBack = (ImageView) findViewById(R.id.imageViewBack);
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        textViewAssetsTitle = (TextView) findViewById(R.id.textViewAssetsTitle);
        textViewAccount = (TextView) findViewById(R.id.textViewAccount);
        textViewWalletNameContent = (TextView) findViewById(R.id.textViewWalletNameContent);
        textViewWalletAddressContent = (TextView) findViewById(R.id.textViewWalletAddressContent);
        relativeLayoutWalletAddress = (RelativeLayout) findViewById(R.id.relativeLayoutWalletAddress);
        relativeLayoutPasswordHint = (RelativeLayout) findViewById(R.id.relativeLayoutPasswordHint);
        relativeLayoutExportMnemonicWords = (RelativeLayout) findViewById(R.id.relativeLayoutExportMnemonicWords);
        relativeLayoutExportPrivateKeys = (RelativeLayout) findViewById(R.id.relativeLayoutExportPrivateKeys);
        relativeLayoutDeleteWallet = (RelativeLayout) findViewById(R.id.relativeLayoutDeleteWallet);
        onClickListener();
    }

    @Override
    protected void initConfigUi() {
        textViewTitle.setText(R.string.activity_wallet_details_textViewTitle_text);
        if (wallet != null) {
            String balance = wallet.getBalance();
            if (balance == null) {
                balance = getString(R.string.number_amount_0_text);
            }
            textViewAssetsTitle.setText(wallet.getSymbol());
            textViewAccount.setText(balance);
            textViewWalletNameContent.setText(wallet.getName());
            textViewWalletAddressContent.setText(wallet.getAddress());
        }
    }

    @Override
    protected void initHttp() {

    }

    @Override
    protected void initOther() {

    }

    private void onClickListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.imageViewBack:
                        doBack();
                        break;
                    case R.id.relativeLayoutWalletAddress:
                        ClipboardTool.copyToClipboard(WalletDetailsActivity.this, textViewWalletAddressContent.getText().toString());
                        break;
                    case R.id.relativeLayoutPasswordHint:
                        showPasswordHint(relativeLayoutPasswordHint);
                        break;
                    case R.id.relativeLayoutExportMnemonicWords:
                        showPassword(relativeLayoutExportMnemonicWords, TYPE_SHOW_MNEMONIC_WORDS);
                        break;
                    case R.id.relativeLayoutExportPrivateKeys:
                        showPassword(relativeLayoutExportMnemonicWords, TYPE_SHOW_PRIVATE_KEYS);
                        break;
                    case R.id.relativeLayoutDeleteWallet:
                        deleteWallet();
                        break;
                    default:
                        break;
                }
            }
        };
        imageViewBack.setOnClickListener(onClickListener);
        relativeLayoutWalletAddress.setOnClickListener(onClickListener);
        relativeLayoutPasswordHint.setOnClickListener(onClickListener);
        relativeLayoutExportMnemonicWords.setOnClickListener(onClickListener);
        relativeLayoutExportPrivateKeys.setOnClickListener(onClickListener);
        relativeLayoutDeleteWallet.setOnClickListener(onClickListener);
    }


    private void showPasswordHint(View view) {
        if (wallet == null) {
            return;
        }
        PopupWindowHelp.showPopupWindowPasswordHintTips(WalletDetailsActivity.this, view.getRootView(), view, wallet.getPasswordPrompt());
    }


    public void showPassword(View view, final int type) {
        if (wallet == null) {
            BaseActivity.showShortToast(WalletDetailsActivity.this, getString(R.string.wallet_not_exists_tips));
            return;
        }
        PopupWindowHelp.showPopupWindowPasswordTips(WalletDetailsActivity.this, view.getRootView(), view, new PopupWindowHelp.OnShowPopupWindowPasswordTipsListener() {
            @Override
            public void okClick(final PopupWindow popupWindow, EditText editTextPassword, View view) {
                final String password = editTextPassword.getText().toString();
                popupWindow.dismiss();
                if (password.equals("")) {
                    BaseActivity.showShortToast(WalletDetailsActivity.this, getString(R.string.password_can_not_empty_tips));
                    return;
                }
                WalletServiceHelper.verifyWalletPasswordOnly(WalletDetailsActivity.this, wallet.getName(), password, new WalletServiceHelper.OnVerifyWalletPasswordListener() {
                    @Override
                    public void onSuccess(String tips) {
                        if (type == TYPE_SHOW_ADDRES) {

                        } else if (type == TYPE_SHOW_MNEMONIC_WORDS) {
                            Intent intent = new Intent(WalletDetailsActivity.this,
                                    WalletDetailsMnemonicWordsActivity.class);
                            intent.putExtra(ActivityHelp.WALLET_KEY, wallet);
                            intent.putExtra(ActivityHelp.SET_WALLET_PASSWORD_KEY, password);
                            startActivity(intent);
                        } else if (type == TYPE_SHOW_PRIVATE_KEYS) {
                            Intent intent = new Intent(WalletDetailsActivity.this,
                                    WalletDetailsPrivateKeysActivity.class);
                            intent.putExtra(ActivityHelp.WALLET_KEY, wallet);
                            intent.putExtra(ActivityHelp.SET_WALLET_PASSWORD_KEY, password);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        BaseActivity.showShortToast(WalletDetailsActivity.this, error);
                    }
                });
            }
        });
    }

    private void deleteWallet() {
        if (wallet == null) {
            return;
        }
        WalletOperateManager walletOperateManager = TheApplication.getTheApplication().getWalletServiceHelper().getWalletOperateManager();
        if (walletOperateManager == null) {
            return;
        }
        Object[] objects = ProgressDialogHelp.unEnabledView(WalletDetailsActivity.this, null);
        final ProgressDialog progressDialog = (ProgressDialog) objects[0];
        final String progressDialogKey = (String) objects[1];
        try {
            walletOperateManager.closeWallet(wallet.getId(), new OnNormalListener.Stub() {
                @Override
                public void onSuccess(final String tips) throws RemoteException {
                    try {
                        XManager.getInstance().deleteWallet(wallet.getName());
                        // delete transactionInfos
                        List<TransactionInfo> transactionInfos = AppDatabase.getInstance().transactionInfoDao().loadTransactionInfoByWalletId(wallet.getSymbol(), wallet.getId());
                        if (transactionInfos != null) {
                            AppDatabase.getInstance().transactionInfoDao().deleteTransactionInfo(transactionInfos.toArray(new TransactionInfo[]{}));
                        }
                        List<Wallet> wallets = AppDatabase.getInstance().walletDao().loadWalletsBySymbol(wallet.getSymbol());
                        // delete wallet
                        AppDatabase.getInstance().walletDao().deleteWallets(wallet);
                        DaoTool.removeWalletById(wallets, wallet.getId());
                        if (wallet.isActive() && wallets != null && wallets.size() > 0) {
                            Wallet wallet = wallets.get(0);
                            wallet.setActive(true);
                            AppDatabase.getInstance().walletDao().updateWallets(wallet);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            BaseActivity.showShortToast(WalletDetailsActivity.this, getString(R.string.activity_wallet_details_deleteSuccess_tips));
                            WalletManagerActivity.doRefreshIfActivityExist();
                            MainActivity.doRefreshIfActivityExist();
                            ProgressDialogHelp.enabledView(WalletDetailsActivity.this, progressDialog, progressDialogKey, null);
                            finish();
                        }
                    });
                }

                @Override
                public void onError(final String error) throws RemoteException {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ProgressDialogHelp.enabledView(WalletDetailsActivity.this, progressDialog, progressDialogKey, null);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            ProgressDialogHelp.enabledView(WalletDetailsActivity.this, progressDialog, progressDialogKey, null);
        }

    }

    @Override
    protected void doBack() {
        super.doBack();
        finish();
    }

}