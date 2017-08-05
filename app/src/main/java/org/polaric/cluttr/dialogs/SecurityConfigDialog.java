package org.polaric.cluttr.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Switch;

import org.polaric.cluttr.R;
import org.polaric.cluttr.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SecurityConfigDialog extends Dialog {
    @BindView(R.id.security_dialog_edittext) EditText editText;
    @BindView(R.id.security_dialog_switch) Switch switchToggle;
    @BindView(R.id.security_dialog_toolbar) Toolbar toolbar;

    public SecurityConfigDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_security_config);
        ButterKnife.bind(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        switchToggle.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("hidden_secure", false));
        editText.setText(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Util.PASSWORD_KEY,""));

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    switchToggle.setChecked(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @OnClick(R.id.security_dialog_cancel)
    public void close() {
        dismiss();
    }

    @OnClick(R.id.security_dialog_apply)
    public void apply() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putString(Util.PASSWORD_KEY, editText.getText().toString());
        editor.putBoolean("hidden_secure", switchToggle.isChecked());
        editor.apply();
        dismiss();
    }
}
