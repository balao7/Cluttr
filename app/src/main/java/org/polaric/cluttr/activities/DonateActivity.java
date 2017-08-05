package org.polaric.cluttr.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import org.polaric.cluttr.R;
import org.polaric.cluttr.Util;
import org.polaric.colorful.ColorfulActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DonateActivity extends ColorfulActivity implements View.OnClickListener, BillingProcessor.IBillingHandler {
    @BindView(R.id.donate_toolbar) Toolbar bar;
    BillingProcessor bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);
        ButterKnife.bind(this);

        bar.setTitle(R.string.donate);
        bar.setNavigationIcon(R.drawable.md_nav_back);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.onedonate).setOnClickListener(this);
        findViewById(R.id.twodonate).setOnClickListener(this);
        findViewById(R.id.fivedonate).setOnClickListener(this);
        findViewById(R.id.tendonate).setOnClickListener(this);
        findViewById(R.id.fifteendonate).setOnClickListener(this);

        bp = new BillingProcessor(this, Util.BILLING_KEY , this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.onedonate:
                bp.purchase(this, "purchase_one");
                break;
            case R.id.twodonate:
                bp.purchase(this, "purchase_two");
                break;
            case R.id.fivedonate:
                bp.purchase(this, "purchase_five");
                break;
            case R.id.tendonate:
                bp.purchase(this, "purchase_ten");
                break;
            case R.id.fifteendonate:
                bp.purchase(this, "purchase_fifteen");
                break;
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.d(Util.LOG_TAG,"Billing error, code " + error);
    }

    @Override
    public void onBillingInitialized() {
        Log.d(Util.LOG_TAG,"Billing Initialized");
    }

    @Override
    public void onPurchaseHistoryRestored() {
        Log.d(Util.LOG_TAG,"Purchase history restored");
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Log.d(Util.LOG_TAG,"Purchase successful");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();
        Log.i(Util.LOG_TAG,"Billing released");
        super.onDestroy();
    }
}