package org.polaric.cluttr.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.polaric.cluttr.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActionMessageDialog extends Dialog {
    @BindView(R.id.action_dialog_toolbar) Toolbar toolbar;
    @BindView(R.id.action_dialog_message) TextView msg;
    @BindView(R.id.action_dialog_ok) Button ok;
    @BindView(R.id.action_dialog_cancel) Button cancel;

    private String buttonText=null, negativeText, titleText;
    private Spanned msgText;

    private OnPositiveListener listener;

    public ActionMessageDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_action_message);
        ButterKnife.bind(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_48px);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        toolbar.setTitle(titleText);
        msg.setText(msgText);
        if (buttonText==null) {
            ok.setVisibility(View.GONE);
        } else {
            ok.setText(buttonText);
        }
        if (negativeText!=null) {
            cancel.setText(negativeText);
        }
    }

    public void setMsgText(String text) {
        msgText= SpannedString.valueOf(text);
    }

    public void setMsgText(Spanned text) {
        msgText = text;
    }

    public void setTitleText(String text) {
        titleText=text;
    }


    public void setPositiveButtonMsg(String msg) {
        buttonText=msg;
    }


    public void setNegativeButtonMsg(String msg) {
        negativeText=msg;
    }

    @OnClick(R.id.action_dialog_cancel)
    public void close() {
        dismiss();
    }

    @OnClick(R.id.action_dialog_ok)
    public void click() {
        if (listener!=null) {
            listener.onPositive();
            dismiss();
        }
    }

    public void setOnPositiveListener(OnPositiveListener listener) {
        this.listener = listener;
    }

    public interface OnPositiveListener {
        void onPositive();
    }
}
