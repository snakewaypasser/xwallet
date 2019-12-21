/**
 * Copyright (c) 2019 by snakeway
 * <p>
 * All rights reserved.
 */
package com.my.adapters.recyclerviewadapter;

import android.content.res.TypedArray;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.my.base.BaseActivity;
import com.my.base.recyclerviewlibrary.adapters.UnLoadMoreRecyclerViewAdapter;
import com.my.base.recyclerviewlibrary.models.ViewItem;
import com.my.utils.StringTool;
import com.my.utils.database.entity.TransactionInfo;
import com.my.xwallet.R;
import com.my.xwallet.aidl.manager.XManager;

import java.util.List;

public class Transaction_Default_RecyclerViewAdapter extends UnLoadMoreRecyclerViewAdapter {
    private BaseActivity baseActivity;
    private RecyclerView recyclerView;
    private int mainColorText;
    private int layout_transaction_item_pay_in;
    private int layout_transaction_item_pay_out;
    private int editText_normal_hint;
    private OnTransactionDefaultListener onTransactionDefaultListener;

    public Transaction_Default_RecyclerViewAdapter(BaseActivity baseActivity, RecyclerView recyclerView, List<ViewItem> datas) {
        super(datas);
        init(baseActivity, recyclerView);
    }

    private void init(BaseActivity baseActivity, RecyclerView recyclerView) {
        setLoadCompletedLayoutId(R.layout.normal_load_completed);
        this.baseActivity = baseActivity;
        this.recyclerView = recyclerView;

        TypedArray typedArray = this.baseActivity.getTheme().obtainStyledAttributes(new int[]{R.attr.mainColorText});
        try {
            mainColorText = typedArray.getColor(0, 0xffffffff);
        } finally {
            typedArray.recycle();
        }
        layout_transaction_item_pay_in = ContextCompat.getColor(this.baseActivity, R.color.layout_transaction_item_pay_in);
        layout_transaction_item_pay_out = ContextCompat.getColor(this.baseActivity, R.color.layout_transaction_item_pay_out);
        editText_normal_hint = ContextCompat.getColor(this.baseActivity, R.color.editText_normal_hint);
    }

    @Override
    public int getNormalLayoutId(int viewType) {
        if (viewType == ViewItem.VIEW_TYPE_NORMAL_ITEM_TYPE1) {
            return R.layout.layout_transaction_item;
        } else {
            return R.layout.base_recyclerview_lostviewtype_item;
        }
    }


    @Override
    public void onBindNormalViewHolder(final BaseRecyclerViewHolder holder, final int position) {
        ViewItem viewItem = getDatas().get(position);
        if (viewItem == null) {
            return;
        }
        if (viewItem.getViewType() == ViewItem.VIEW_TYPE_NORMAL_ITEM_TYPE1) {
            final TransactionInfo transactionInfo = (TransactionInfo) viewItem.getModel();
            if (transactionInfo != null) {
                TextView textViewStatus = (TextView) holder.getView(R.id.textViewStatus);
                TextView textViewAmount = (TextView) holder.getView(R.id.textViewAmount);
                TextView textViewHash = (TextView) holder.getView(R.id.textViewHash);
                TextView textViewTime = (TextView) holder.getView(R.id.textViewTime);
                if (transactionInfo.isPending() || transactionInfo.getConfirmations() < XManager.TRANSACTION_MIN_CONFIRMATION) {
                    textViewStatus.setText(baseActivity.getString(R.string.layout_transaction_item_status_tips) + "(" + baseActivity.getString(R.string.layout_transaction_item_status_pending_tips) + " " + transactionInfo.getConfirmations() + ")");
                    textViewStatus.setTextColor(editText_normal_hint);
                } else {
                    textViewStatus.setText(baseActivity.getString(R.string.layout_transaction_item_status_tips) + "(" + baseActivity.getString(R.string.layout_transaction_item_status_over_tips) + ")");
                    textViewStatus.setTextColor(mainColorText);
                }
                String amount = transactionInfo.getAmount();
                if (transactionInfo.getDirection() == 1) {
                    textViewAmount.setText("-" + amount);
                    textViewAmount.setTextColor(layout_transaction_item_pay_out);
                } else {
                    textViewAmount.setText("+" + amount);
                    textViewAmount.setTextColor(layout_transaction_item_pay_in);
                }
                textViewHash.setText(baseActivity.getString(R.string.layout_transaction_item_hash_tips) + "    " + transactionInfo.getHash());
                textViewTime.setText(baseActivity.getString(R.string.layout_transaction_item_date_tips) + "    " + StringTool.getDateTime(transactionInfo.getTimestamp()));

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onTransactionDefaultListener != null) {
                            onTransactionDefaultListener.onItemSelect(transactionInfo);
                        }
                    }
                });
            }
        }
    }

    public OnTransactionDefaultListener getOnTransactionDefaultListener() {
        return onTransactionDefaultListener;
    }

    public void setOnTransactionDefaultListener(OnTransactionDefaultListener onTransactionDefaultListener) {
        this.onTransactionDefaultListener = onTransactionDefaultListener;
    }

    public interface OnTransactionDefaultListener {
        void onItemSelect(TransactionInfo transactionInfo);
    }
}