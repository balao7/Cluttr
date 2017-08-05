package org.polaric.cluttr.activities;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.test.mock.MockApplication;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.digitus.FingerprintDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;

import org.polaric.cluttr.Cluttr;
import org.polaric.cluttr.R;
import org.polaric.cluttr.Util;
import org.polaric.cluttr.data.Media;
import org.polaric.cluttr.dialogs.ExcludedFoldersDialog;
import org.polaric.cluttr.dialogs.SecurityConfigDialog;
import org.polaric.cluttr.io.ExtSdHelper;
import org.polaric.cluttr.preference.ExcludeFolderPreference;
import org.polaric.colorful.ColorPickerPreference;
import org.polaric.colorful.Colorful;
import org.polaric.colorful.ColorfulActivity;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends ColorfulActivity implements View.OnClickListener, FolderChooserDialog.FolderCallback, FingerprintDialog.Callback {
    @BindView(R.id.settings_toolbar) Toolbar mToolbar;

    private OnFolderSelectedListener l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_48px);
        mToolbar.setNavigationOnClickListener(this);

        SettingsFragment fragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_frame, fragment).commit();
    }

    public void openFingerprintDialog() {
        FingerprintDialog.show(this, Util.FINGERPRINT_KEY, Util.PERMISSION_REQUEST_FINGERPRINT);
    }

    @Override
    public void onFingerprintDialogAuthenticated() {
        new SecurityConfigDialog(this).show();
    }

    @Override
    public void onFingerprintDialogVerifyPassword(FingerprintDialog dialog, final String password) {
        dialog.notifyPasswordValidation(password.equals(PreferenceManager.getDefaultSharedPreferences(this).getString(Util.PASSWORD_KEY,null)));
    }

    @Override
    public void onFingerprintDialogStageUpdated(FingerprintDialog dialog, FingerprintDialog.Stage stage) {
        Log.d(Util.LOG_TAG, "Fingerprint stage: " + stage.name());
    }

    @Override
    public void onFingerprintDialogCancelled() {
        Toast.makeText(this, "Authentication cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        if (l!=null) {
            Media.init(this, false);
            Media.invalidate();
            l.onFolderSelected(folder);
        }
    }

    public interface OnFolderSelectedListener {
        public abstract void onFolderSelected(File folder);
    }

    public void setOnFolderSelectedListener(OnFolderSelectedListener l) {
        this.l=l;
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            switch (preference.getKey()) {
                case "dark_theme": {
                    Colorful.config(getContext())
                            .dark(((boolean) o))
                            .apply();
                    getActivity().recreate();
                    break;
                }
                case "animations": {
                    Media.setUseAnimations((boolean) o);
                    break;
                }
                case "staggered": {
                    Media.setStaggered((boolean) o);
                }
                case "primary" : {
                    getActivity().recreate();
                    break;
                }
                case "accent" : {
                    getActivity().recreate();
                    break;
                }

            }
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case "excluded_folders_listener_key": {
                    ExcludedFoldersDialog d = new ExcludedFoldersDialog(getActivity());
                    d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            getActivity().recreate();
                        }
                    });
                    d.show();
                    break;
                }
                case "security_placeholder": {
                    if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("hidden_secure", false)) {
                        ((SettingsActivity) getActivity()).openFingerprintDialog();
                    } else {
                        new SecurityConfigDialog(getContext()).show();
                    }
                    break;
                }
                case "clear_cache_placeholder": {
                    Media.invalidate();
                    Toast.makeText(getActivity(), R.string.cache_clear_done, Toast.LENGTH_LONG).show();
                    break;
                }
                case "externalsd_placeholder": {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.sd_title)
                            .setMessage(R.string.sd_explanation)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent saf = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                    startActivityForResult(saf, ExtSdHelper.SAF_REQUEST);
                                }
                            })
                            .show();
                    break;
                }
                case "removesd_placeholder": {
                    ExtSdHelper.clear();
                    Toast.makeText(getContext(), R.string.remove_complete, Toast.LENGTH_LONG).show();
                    break;
                }
            }
            return false;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_OK && requestCode==ExtSdHelper.SAF_REQUEST) {
                Uri treeUri = data.getData();
                ExtSdHelper.setSdPath(treeUri.toString());
                invalidateSettings();
            }
        }

        public void invalidateSettings() {
            SwitchPreference dark = (SwitchPreference) findPreference("dark_theme");
            ExcludeFolderPreference exclude = (ExcludeFolderPreference) findPreference("excluded_folders_listener_key");
            ColorPickerPreference primary = (ColorPickerPreference) findPreference("primary");
            ColorPickerPreference accent = (ColorPickerPreference) findPreference("accent");
            Preference security = findPreference("security_placeholder");
            Preference animations = findPreference("animations");
            Preference staggered = findPreference("staggered");
            Preference cache = findPreference("clear_cache_placeholder");
            Preference sdcard = findPreference("externalsd_placeholder");
            Preference removesd = findPreference("removesd_placeholder");

            sdcard.setSummary(ExtSdHelper.getSdPath()==null ? getContext().getResources().getString(R.string.no_sd) : ExtSdHelper.getSdPath().substring(ExtSdHelper.getSdPath().lastIndexOf("/")+1));

            if (Build.VERSION.SDK_INT < 21) {
                sdcard.setEnabled(false);
            }

            removesd.setOnPreferenceClickListener(this);
            sdcard.setOnPreferenceClickListener(this);
            security.setOnPreferenceClickListener(this);
            exclude.setOnPreferenceClickListener(this);
            cache.setOnPreferenceClickListener(this);
            staggered.setOnPreferenceChangeListener(this);
            animations.setOnPreferenceChangeListener(this);
            dark.setOnPreferenceChangeListener(this);
            primary.setOnPreferenceChangeListener(this);
            accent.setOnPreferenceChangeListener(this);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            invalidateSettings();
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }


    }
}