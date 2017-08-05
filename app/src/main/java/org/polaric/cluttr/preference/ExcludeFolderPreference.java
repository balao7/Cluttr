package org.polaric.cluttr.preference;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.polaric.cluttr.R;

import java.util.ArrayList;
import java.util.Arrays;

public class ExcludeFolderPreference extends Preference {
    private TextView preview;
    private String prefString;
    private ArrayList<String> folders;

    public ExcludeFolderPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWidgetLayoutResource(R.layout.preference_exclude_folder);
        loadPref();
    }

    public ExcludeFolderPreference(Context context) {
        super(context);
        setWidgetLayoutResource(R.layout.preference_exclude_folder);
        loadPref();
    }

    public ExcludeFolderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_exclude_folder);
        loadPref();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        preview = ((TextView) holder.findViewById(R.id.folder_indicator));
        preview.setText(folders.size() + " " + getContext().getResources().getString(R.string.excluded));
    }

    private void loadPref() {
        prefString = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("excluded_folders","");
        if (prefString.isEmpty()) {
            folders = new ArrayList<>();
        } else {
            folders = new ArrayList<>(Arrays.asList(prefString.split(":")));
        }
    }
}
