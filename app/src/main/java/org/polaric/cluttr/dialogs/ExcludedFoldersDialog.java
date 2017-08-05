package org.polaric.cluttr.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;

import org.polaric.cluttr.R;
import org.polaric.cluttr.activities.SettingsActivity;
import org.polaric.cluttr.adapters.ExcludedFoldersAdapter;
import org.polaric.cluttr.data.Media;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class ExcludedFoldersDialog extends Dialog implements View.OnClickListener, Action1<Integer>, SettingsActivity.OnFolderSelectedListener {
    @BindView(R.id.exclude_folders_toolbar) Toolbar toolbar;
    @BindView(R.id.exclude_folders_recycler) RecyclerView recycler;
    @BindView(R.id.exclude_folders_fab) FloatingActionButton fab;

    private ExcludedFoldersAdapter adapter;
    private String prefString;
    private ArrayList<String> folders;
    private SettingsActivity reference;
    private Context context;

    public ExcludedFoldersDialog(Context context) {
        super(context);
        reference= (SettingsActivity) context;
        reference.setOnFolderSelectedListener(this);
        this.context=context;
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }

    @Override
    public void call(final Integer integer) {
        new AlertDialog.Builder(getContext())
                .setItems(new String[]{getContext().getResources().getString(R.string.remove)}, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        folders.remove(((int) integer));
                        updatePrefString();
                        adapter.notifyDataSetChanged();
                        Media.init(getContext(), false);
                        Media.invalidate();
                    }
                })
                .show();
    }

    @Override
    public void onFolderSelected(File folder) {
        folders.add(folder.getAbsolutePath());
        updatePrefString();
        adapter.update(folders);
    }

    private void updatePrefString() {
        prefString="";
        for (String f : folders) {
            if (!prefString.isEmpty()) {
                prefString+=":";
            }
            prefString+=f;
        }
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("excluded_folders",prefString).commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_excluded_folders);
        ButterKnife.bind(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        toolbar.setTitle(R.string.exclude_folders);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_48px);
        toolbar.setNavigationOnClickListener(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FolderChooserDialog.Builder(reference)
                        .chooseButton(R.string.md_choose_label)
                        .show();
            }
        });

        prefString = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("excluded_folders","");
        if (prefString.isEmpty()) {
            folders = new ArrayList<>();
        } else {
            folders = new ArrayList<>(Arrays.asList(prefString.split(":")));
        }

        adapter = new ExcludedFoldersAdapter(getContext());
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);
        adapter.update(folders);

        adapter.getOnClickObservable().subscribe(this);

    }

}
