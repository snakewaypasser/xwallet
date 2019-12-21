/**
 * Copyright (c) 2019 by snakeway
 * <p>
 * All rights reserved.
 */
package com.my.utils;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import com.my.base.BaseActivity;
import com.my.xwallet.MainActivity;
import com.my.xwallet.R;
import com.my.xwallet.TheApplication;
import com.my.xwallet.WalletRunningActivity;
import com.my.xwallet.aidl.OnNormalListener;
import com.my.xwallet.aidl.OnWalletRefreshListener;
import com.my.xwallet.aidl.WalletOperateManager;
import com.my.xwallet.aidl.service.WalletService;
import com.my.xwallet.uihelp.ProgressDialogHelp;

public class WalletServiceHelper {

    private Handler handler = new Handler(Looper.getMainLooper());
    private WalletOperateManager walletOperateManager;
    private OnWalletRefreshListener onWalletRefreshListener = new MyOnWalletRefreshListener();

    private Context context;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        /**
         * Running in Main thread
         */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            walletOperateManager = WalletOperateManager.Stub.asInterface(iBinder);
            try {
                walletOperateManager.registerListener(onWalletRefreshListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        /**
         * Running in Main thread
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            BaseActivity.showSystemErrorLog(componentName + ":onServiceDisconnected...");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindService();
                }
            }, 500);
        }
    };


    public WalletServiceHelper(Context context) {
        this.context = context.getApplicationContext();
    }


    public void bindService() {
        Intent intent = new Intent(context, WalletService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService() {
        if (walletOperateManager != null && walletOperateManager.asBinder().isBinderAlive()) {
            try {
                walletOperateManager.unRegisterListener(onWalletRefreshListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            walletOperateManager = null;
        }
        context.unbindService(serviceConnection);
    }

    public WalletOperateManager getWalletOperateManager() {
        if (walletOperateManager == null) {
            BaseActivity.showLongToast(TheApplication.getTheApplication(), TheApplication.getTheApplication().getString(R.string.service_uninitialized_tips));
        }
        return walletOperateManager;
    }

    /**
     * All functions running in thread
     */
    class MyOnWalletRefreshListener extends OnWalletRefreshListener.Stub {
        @Override
        public void queueFullError(final String error) throws RemoteException {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    BaseActivity.showShortToast(context, error);
                }
            });
        }

        @Override
        public void beginLoadWallet(final int walletId) throws RemoteException {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.beginLoadWalletIfActivityExist(walletId);
                    WalletRunningActivity.beginLoadWalletIfActivityExist(walletId);
                }
            });
        }

        @Override
        public void synchronizeStatusError(final int walletId, final String error) throws RemoteException {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.showSynchronizeStatusErrorIfActivityExist(walletId, error);
                    WalletRunningActivity.showSynchronizeStatusErrorIfActivityExist(walletId, error);
                }
            });
        }

        @Override
        public void synchronizeStatusSuccess(final int walletId) throws RemoteException {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.synchronizeStatusSuccessIfActivityExist(walletId);
                    WalletRunningActivity.synchronizeStatusSuccessIfActivityExist(walletId);
                }
            });
        }

        @Override
        public void refreshBalance(final int walletId, final String balance, final String unlockedBalance) throws RemoteException {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.refreshBalanceIfActivityExist(walletId, balance, unlockedBalance);
                    WalletRunningActivity.refreshBalanceIfActivityExist(walletId, balance, unlockedBalance);
                }
            });
        }

        @Override
        public void blockProgress(final int walletId, final boolean result, final long blockChainHeight, final long daemonHeight, final int progress) throws RemoteException {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.showBlockProgressIfActivityExist(walletId, result, blockChainHeight, daemonHeight, progress);
                    WalletRunningActivity.showBlockProgressIfActivityExist(walletId, result, blockChainHeight, daemonHeight, progress);
                }
            });
        }

        @Override
        public void refreshTransaction(final int walletId) throws RemoteException {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.refreshTransactionIfActivityExist(walletId);
                    WalletRunningActivity.refreshTransactionIfActivityExist(walletId);
                }
            });
        }

        @Override
        public void closeActiveWallet(final int walletId) throws RemoteException {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.closeWalletIfActivityExist(walletId);
                }
            });
        }
    }


    public static void verifyWalletPasswordOnly(final BaseActivity baseActivity, String name, String password, final OnVerifyWalletPasswordListener onVerifyWalletPasswordListener) {
        if (name == null || password == null || onVerifyWalletPasswordListener == null || baseActivity.handler == null) {
            return;
        }
        WalletOperateManager walletOperateManager = TheApplication.getTheApplication().getWalletServiceHelper().getWalletOperateManager();
        if (walletOperateManager == null) {
            return;
        }
        Object[] objects = ProgressDialogHelp.unEnabledView(baseActivity, null);
        final ProgressDialog progressDialog = (ProgressDialog) objects[0];
        final String progressDialogKey = (String) objects[1];
        try {
            walletOperateManager.checkWalletPassword(name, password, new OnNormalListener.Stub() {
                @Override
                public void onSuccess(final String tips) throws RemoteException {
                    baseActivity.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onVerifyWalletPasswordListener.onSuccess(tips);
                            ProgressDialogHelp.enabledView(baseActivity, progressDialog, progressDialogKey, null);

                        }
                    });
                }

                @Override
                public void onError(final String error) throws RemoteException {
                    baseActivity.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onVerifyWalletPasswordListener.onError(error);
                            ProgressDialogHelp.enabledView(baseActivity, progressDialog, progressDialogKey, null);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            onVerifyWalletPasswordListener.onError(baseActivity.getString(R.string.wallet_service_not_running_tips));
            ProgressDialogHelp.enabledView(baseActivity, progressDialog, progressDialogKey, null);
        }
    }


    public interface OnVerifyWalletPasswordListener {

        void onSuccess(String tips);

        void onError(String error);
    }


}