package org.polaric.cluttr.preference;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.polaric.cluttr.R;
import org.polaric.cluttr.data.Media;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DualNumberPickerPreference extends Preference {
    private int valueOne,valueTwo,maxOne,maxTwo;
    private TextView summary;
    private String titleOne,titleTwo,keyOne,keyTwo;

    public DualNumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
        setWidgetLayoutResource(R.layout.preference_dualpicker);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DualNumberPicker, 0, 0);
        try {
            valueOne = ta.getInteger(R.styleable.DualNumberPicker_default_one, 1);
            valueTwo = ta.getInteger(R.styleable.DualNumberPicker_default_two, 1);
            maxOne = ta.getInteger(R.styleable.DualNumberPicker_max_one, 20);
            maxTwo = ta.getInteger(R.styleable.DualNumberPicker_max_two, 20);
            titleOne = ta.getString(R.styleable.DualNumberPicker_title_one);
            titleTwo = ta.getString(R.styleable.DualNumberPicker_title_two);
            keyOne = ta.getString(R.styleable.DualNumberPicker_key_one);
            keyTwo = ta.getString(R.styleable.DualNumberPicker_key_two);
        } finally {
            ta.recycle();
        }
    }




    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        summary = ((TextView) holder.findViewById(R.id.preview_indicator));
        updateSummary();
    }


    private void updateSummary() {
        if (summary!=null) {
            valueOne = getSharedPreferences().getInt(keyOne,valueOne);
            valueTwo = getSharedPreferences().getInt(keyTwo,valueTwo);
            summary.setText(valueOne + " x " + valueTwo);
        }
    }

    @Override
    protected void onClick() {
        super.onClick();
        PreferenceDialog dialog = new PreferenceDialog(getContext(), valueOne, valueTwo, maxOne, maxTwo, titleOne, titleTwo, keyOne, keyTwo);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                updateSummary();
            }
        });
        dialog.show();
    }

    public static class PreferenceDialog extends Dialog {
        private int valueOne,valueTwo,maxOne,maxTwo;
        private String titleOne,titleTwo,keyOne,keyTwo;
        @BindView(R.id.picker_one) NumberPicker pickerOne;
        @BindView(R.id.picker_two) NumberPicker pickerTwo;
        @BindView(R.id.text_one) TextView textOne;
        @BindView(R.id.text_two) TextView textTwo;

        public PreferenceDialog(Context context, int valueOne, int valueTwo, int maxOne, int maxTwo, String titleOne, String titleTwo, String keyOne, String keyTwo) {
            super(context);
            this.valueOne = valueOne;
            this.valueTwo = valueTwo;
            this.maxOne = maxOne;
            this.maxTwo = maxTwo;
            this.titleOne = titleOne;
            this.titleTwo = titleTwo;
            this.keyOne = keyOne;
            this.keyTwo = keyTwo;
        }

        @OnClick(R.id.dualpicker_cancel)
        public void close() {
            dismiss();
        }

        @OnClick(R.id.dualpicker_ok)
        public void confirm() {
            valueOne=pickerOne.getValue();
            valueTwo=pickerTwo.getValue();
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                    .putInt(keyOne,valueOne)
                    .putInt(keyTwo,valueTwo)
                    .commit();
            Media.init(getContext(),true);
            dismiss();
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.preference_dualpicker_dialog);
            ButterKnife.bind(this);

            pickerOne.setMinValue(1);
            pickerTwo.setMinValue(1);
            pickerOne.setMaxValue(maxOne);
            pickerTwo.setMaxValue(maxTwo);
            pickerOne.setValue(valueOne);
            pickerTwo.setValue(valueTwo);
            textOne.setText(titleOne);
            textTwo.setText(titleTwo);
        }
    }

}